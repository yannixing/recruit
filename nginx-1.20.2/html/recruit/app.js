(function () {
  "use strict";

  var API_BASE = "/api";
  var PORTAL = location.pathname.indexOf("/admin") === 0 ? "admin" : "public";
  var STORAGE_PREFIX = PORTAL === "admin" ? "recruit-admin" : "recruit-public";
  var jobs = [];
  var hotJobs = [];
  var applications = [];
  var resumes = [];
  var interviews = [];
  var notifications = [];
  var users = [];
  var pendingJobRecords = [];
  var company = {};
  var dashboard = {};
  var loadedKey = "";

  function storageKey(name) {
    return STORAGE_PREFIX + "-" + name;
  }

  function readStorage(name) {
    return localStorage.getItem(storageKey(name)) || "";
  }

  function writeStorage(name, value) {
    localStorage.setItem(storageKey(name), value);
  }

  function removeStorage(name) {
    localStorage.removeItem(storageKey(name));
  }

  var state = {
    role: PORTAL === "admin" ? "admin" : readStorage("role"),
    userName: readStorage("user"),
    token: readStorage("token"),
    userId: readStorage("user-id"),
    route: location.hash.replace("#", "") || (PORTAL === "admin" ? "/admin/dashboard" : "/login"),
    jobId: null,
    currentJob: null,
    modal: null,
    search: "",
    city: "",
    searchDraft: null,
    cityDraft: null,
    status: "",
    page: 1,
    pageSize: 10,
    total: 0,
    platformJobTotal: 0
  };

  var roleNames = { candidate: "求职者", hr: "HR", admin: "管理员" };
  var jobStatus = {
    "0": ["待审核", "pending"],
    "1": ["招聘中", "approved"],
    "2": ["已驳回", "rejected"],
    "3": ["已下架", "offline"]
  };
  var applicationStatus = {
    "0": ["待沟通", "pending"],
    "1": ["已查看", "interview"],
    "2": ["邀约面试", "interview"],
    "3": ["面试通过", "success"],
    "4": ["发送 Offer", "success"],
    "5": ["已入职", "success"],
    "6": ["不合适", "rejected"],
    "7": ["已拒绝", "rejected"]
  };

  function authHeaders() {
    var headers = { "Content-Type": "application/json" };
    if (state.token) {
      headers.token = state.token;
    }
    return headers;
  }

  function apiFetch(path, options) {
    return fetch(API_BASE + path, Object.assign({
      headers: authHeaders()
    }, options || {})).then(function (response) {
      return response.text().then(function (text) {
        var body = null;
        try {
          body = text ? JSON.parse(text) : null;
        } catch (parseError) {
          body = null;
        }
        if (!response.ok || !body || body.code !== 1) {
          throw new Error((body && body.msg) || String(response.status));
        }
        return body.data;
      });
    });
  }

  function formatDate(value) {
    if (!value) return "";
    return String(value).slice(0, 10);
  }

  function formatDateTime(value) {
    if (!value) return "";
    return String(value).replace("T", " ").slice(0, 16);
  }

  function normalizeJob(item) {
    if (!item) return null;
    return {
      id: item.id,
      companyId: item.companyId,
      company: item.companyName || item.company || "",
      shortName: item.shortName,
      industry: item.industry,
      companySize: item.companySize,
      logo: item.logo,
      address: item.address,
      website: item.website,
      companyDescription: item.companyDescription || item.description || "",
      title: item.title,
      category: item.category || "",
      salaryMin: item.salaryMin,
      salaryMax: item.salaryMax,
      city: item.city || "",
      experience: item.workExperience || item.experience || "",
      education: item.education || "",
      description: item.jobDescription || item.description || "",
      requirement: item.requirement || "",
      benefits: item.benefits || "",
      status: item.status,
      deliveryCount: item.deliveryCount || 0,
      viewCount: item.viewCount || 0,
      auditRemark: item.auditRemark || "",
      createdAt: formatDate(item.createTime || item.createdAt),
      createTime: item.createTime
    };
  }

  function normalizeDelivery(item) {
    if (!item) return null;
    return {
      id: item.id,
      jobId: item.jobId,
      candidate: item.candidateName || item.resumeName || "",
      resume: item.resumeName || "",
      status: item.status,
      time: formatDateTime(item.deliveryTime || item.time),
      owner: item.userId,
      job: item.jobTitle || "",
      company: item.companyName || "",
      city: item.city || "",
      salaryMin: item.salaryMin,
      salaryMax: item.salaryMax,
      education: item.education || "",
      school: item.school || "",
      skill: item.skill || "",
      statusRemark: item.statusRemark || ""
    };
  }

  function normalizeResume(item) {
    if (!item) return null;
    return {
      id: item.id,
      name: item.name,
      gender: item.gender,
      birthDate: item.birthDate || "",
      phone: item.phone || "",
      email: item.email || "",
      address: item.address || "",
      school: item.school || "",
      major: item.major || "",
      workExperience: item.workExperience || "",
      projectExperience: item.projectExperience || "",
      selfEvaluation: item.selfEvaluation || "",
      attachment: item.attachment || "",
      updated: formatDate(item.updateTime || item.createTime),
      education: [item.education, item.school].filter(Boolean).join(" · "),
      summary: item.workExperience || item.selfEvaluation || "",
      skills: item.skill || "",
      default: item.isDefault === 1
    };
  }

  function normalizeInterview(item) {
    if (!item) return null;
    return {
      id: item.id,
      deliveryId: item.deliveryId,
      job: item.jobTitle || "",
      company: item.companyName || "",
      time: formatDateTime(item.interviewTime),
      interviewTime: item.interviewTime || "",
      duration: item.duration || 60,
      location: item.location || "",
      addressDetail: item.addressDetail || "",
      contactPerson: item.contactPerson || "",
      contactPhone: item.contactPhone || "",
      remark: item.remark || "",
      feedback: item.feedback || "",
      status: item.status,
      candidate: item.candidateName || "",
      interviewer: item.interviewer || ""
    };
  }

  function interviewStatusTag(status) {
    var map = {
      "0": ["待确认", "pending"],
      "1": ["已确认", "approved"],
      "2": ["已拒绝", "rejected"],
      "3": ["已完成", "success"],
      "4": ["已取消", "offline"]
    };
    return statusTag(status, map);
  }

  function normalizeNotification(item) {
    if (!item) return null;
    return {
      id: item.id,
      title: item.title,
      content: item.content,
      time: formatDateTime(item.createTime),
      unread: item.isRead === 0
    };
  }

  function normalizeUser(item) {
    if (!item) return null;
    return {
      id: item.id,
      name: item.username || "",
      label: item.username || item.email || item.phone || "",
      role: item.role === 0 ? "求职者" : item.role === 1 ? "HR" : "管理员",
      status: item.status,
      state: item.status === 1 ? "正常" : item.status === 2 ? "已注销" : "禁用"
    };
  }

  function roleRoute() {
    if (state.role === "hr") return "/hr";
    if (state.role === "admin") return "/admin";
    return "/candidate";
  }

  function messageRoute() {
    if (state.role === "candidate") return "/candidate/notifications";
    if (state.role === "hr") return "/hr/notifications";
    return "/admin/users";
  }

  function currentJobListPage() {
    if (state.role === "hr") return "/hr/jobs";
    if (state.role === "admin") return "/admin/jobs";
    return "/candidate/jobs";
  }

  function normalizePortalRoute() {
    if (PORTAL === "admin") {
      if (state.route !== "/login" && state.route.indexOf("/admin") !== 0) {
        state.route = "/admin/dashboard";
        location.hash = state.route;
      }
      state.role = "admin";
      return;
    }
    if (state.route.indexOf("/admin") === 0) {
      clearSession();
      location.hash = "/login";
    }
  }

  async function loadRoleData() {
    if (!state.role || !state.token) {
      return;
    }
    var key = [state.role, state.route, state.search, state.city, state.status, state.page, state.pageSize, state.jobId].join("|");
    if (loadedKey === key) {
      return;
    }

    if (state.role === "candidate") {
      var candidateJobs = await apiFetch("/candidate/jobs?page=1&pageSize=200" +
        (state.search ? "&keyword=" + encodeURIComponent(state.search) : "") +
        (state.city ? "&city=" + encodeURIComponent(state.city) : ""));
      jobs = (candidateJobs.records || []).map(normalizeJob);
      hotJobs = (await apiFetch("/candidate/jobs/hot")).map(normalizeJob);
      resumes = (await apiFetch("/candidate/resumes")).map(normalizeResume);
      applications = (await apiFetch("/candidate/deliveries")).records.map(normalizeDelivery);
      interviews = (await apiFetch("/candidate/interviews")).records.map(normalizeInterview);
      notifications = (await apiFetch("/candidate/notifications")).map(normalizeNotification);
    } else if (state.role === "hr") {
      var hrJobs = await apiFetch("/hr/jobs?page=1&pageSize=200" +
        (state.search ? "&keyword=" + encodeURIComponent(state.search) : "") +
        (state.city ? "&city=" + encodeURIComponent(state.city) : "") +
        (state.status ? "&status=" + encodeURIComponent(state.status) : ""));
      jobs = (hrJobs.records || []).map(normalizeJob);
      applications = (await apiFetch("/hr/applications")).records.map(normalizeDelivery);
      interviews = (await apiFetch("/hr/interviews")).records.map(normalizeInterview);
      company = await apiFetch("/hr/company");
      notifications = (await apiFetch("/hr/notifications")).map(normalizeNotification);
      dashboard = await apiFetch("/hr/dashboard");
    } else if (state.role === "admin") {
      if (state.route === "/admin/jobs" || state.route === "/admin/review") {
        var adminPath = state.route === "/admin/jobs" ? "/admin/jobs" : "/admin/reviews";
        var adminPage = await apiFetch(adminPath + "?page=" + state.page + "&pageSize=" + state.pageSize +
          (state.search ? "&keyword=" + encodeURIComponent(state.search) : "") +
          (state.city ? "&city=" + encodeURIComponent(state.city) : ""));
        jobs = (adminPage.records || []).map(normalizeJob);
        state.total = adminPage.total || 0;
      } else {
        jobs = [];
        state.total = 0;
      }
      users = (await apiFetch("/admin/users")).map(normalizeUser);
      dashboard = await apiFetch("/admin/dashboard");
      state.platformJobTotal = dashboard.totalJobs || 0;
      if (state.route === "/admin/dashboard") {
        var pendingPage = await apiFetch("/admin/reviews?page=1&pageSize=5");
        pendingJobRecords = (pendingPage.records || []).map(normalizeJob);
      } else {
        pendingJobRecords = [];
      }
    }

    if (state.jobId) {
      try {
        var detailPath = state.role === "admin" ? "/admin/jobs/" : roleRoute() + "/jobs/";
        state.currentJob = normalizeJob(await apiFetch(detailPath + state.jobId));
      } catch (error) {
        state.currentJob = null;
      }
    }
    loadedKey = key;
  }

  function invalidateData() {
    loadedKey = "";
  }

  function clearSession() {
    ["role", "user", "token", "user-id"].forEach(function (key) {
      removeStorage(key);
    });
    state.role = "";
    state.userName = "";
    state.token = "";
    state.userId = "";
    state.route = "/login";
    state.currentJob = null;
    state.search = "";
    state.city = "";
    state.searchDraft = null;
    state.cityDraft = null;
    state.status = "";
    invalidateData();
  }

  function esc(value) {
    return String(value == null ? "" : value)
      .replace(/&/g, "&amp;").replace(/</g, "&lt;")
      .replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }

  function roleLabel(role) { return roleNames[role] || "访客"; }
  function statusTag(code, map) {
    var item = map[code] || ["未知", ""];
    return '<span class="tag ' + item[1] + '">' + item[0] + "</span>";
  }
  function jobById(id) {
    if (state.currentJob && state.currentJob.id === Number(id)) return state.currentJob;
    return jobs.find(function (job) { return job.id === Number(id); });
  }
  function formatSalary(job) {
    var min = job && job.salaryMin != null ? job.salaryMin : "-";
    var max = job && job.salaryMax != null ? job.salaryMax : "-";
    return "¥" + min + "–" + max + "K";
  }
  function initials(text) { return String(text || "R").slice(0, 2); }
  function optionList(items, current) {
    return items.map(function (item) {
      return '<option ' + (String(item) === String(current) ? 'selected' : '') + '>' + esc(item) + '</option>';
    }).join("");
  }
  function routeTitle() {
    var titles = {
      dashboard: "招聘概览", jobs: "职位管理", applications: "投递管理", interviews: "面试安排",
      company: "企业资料", notifications: "消息中心", discover: "职位发现", deliveries: "我的投递",
      resumes: "我的简历", review: "职位审核", users: "用户管理"
    };
    return titles[state.route.split("/")[2]] || "招聘概览";
  }

  function navItem(route, icon, label) {
    var active = state.route === route || state.route.indexOf(route + "/") === 0 ? " active" : "";
    return '<button class="' + active + '" data-route="' + route + '"><span class="nav-icon">' + icon + "</span><span>" + label + "</span></button>";
  }

  function navigation() {
    if (state.role === "hr") {
      return '<div class="nav-section">工作台</div><div class="nav">' +
        navItem("/hr/dashboard", "⌂", "招聘概览") +
        navItem("/hr/jobs", "▦", "职位管理") +
        navItem("/hr/applications", "☷", "投递管理") +
        navItem("/hr/interviews", "◷", "面试安排") +
        '</div><div class="nav-section">组织</div><div class="nav">' +
        navItem("/hr/company", "◇", "企业资料") +
        navItem("/hr/notifications", "●", "消息中心") + "</div>";
    }
    if (state.role === "admin") {
      return '<div class="nav-section">平台管理</div><div class="nav">' +
        navItem("/admin/dashboard", "⌂", "数据概览") +
        navItem("/admin/review", "✓", "职位审核") +
        navItem("/admin/users", "◎", "用户管理") +
        navItem("/admin/jobs", "▦", "职位管理") + "</div>";
    }
    return '<div class="nav-section">求职中心</div><div class="nav">' +
      navItem("/candidate/discover", "⌕", "职位发现") +
      navItem("/candidate/deliveries", "☷", "我的投递") +
      navItem("/candidate/resumes", "▤", "我的简历") +
      navItem("/candidate/interviews", "◷", "面试邀约") +
      '</div><div class="nav-section">账户</div><div class="nav">' +
      navItem("/candidate/notifications", "●", "消息中心") + "</div>";
  }

  function shell(content) {
    return '<div class="app-shell"><aside class="sidebar" id="sidebar">' +
      '<div class="brand"><span class="brand-mark">R</span><span><strong>Recruit</strong><small>招聘管理系统</small></span></div>' +
      navigation() + '<div class="sidebar-spacer"></div><div class="profile-mini">' +
      '<span class="avatar">' + esc(initials(state.userName || roleLabel(state.role))) + '</span><span><strong>' +
      esc(state.userName || roleLabel(state.role)) + '</strong><small>' + roleLabel(state.role) + '</small></span></div></aside>' +
      '<section class="content"><header class="topbar"><div class="crumb"><button class="icon-button mobile-menu" data-action="toggle-sidebar">≡</button> <span>Recruit / </span><strong>' +
      routeTitle() + '</strong></div><div class="top-actions"><span class="mode-pill">接口数据模式</span><button class="icon-button" data-route="' +
      messageRoute() + '" title="消息">●</button><button class="icon-button" data-action="logout" title="退出">↗</button></div></header><main class="main">' +
      content + "</main></section></div>";
  }

  function loginView() {
    var role = PORTAL === "admin" ? "admin" : (state.role || "candidate");
    var accountLabel = PORTAL === "admin" ? "管理员账号" : (role === "hr" ? "HR 邮箱" : "手机号");
    var accountValue = PORTAL === "admin" ? "admin" : (role === "hr" ? "hr.zhang@tech.com" : "13800000010");
    var introTitle = PORTAL === "admin" ? "管理员内网。" : "让每一次匹配，都更接近理想。";
    var introText = PORTAL === "admin"
      ? "这里是管理员内网入口，只用于平台治理与审核。"
      : "连接企业与人才，把职位、简历、面试和录用放在同一个清晰的工作台里。";
    var introNotes = PORTAL === "admin"
      ? '<div class="login-notes"><span><b>1</b>管理员内网入口</span><span><b>2</b>仅允许管理员账号登录</span><span><b>3</b>与公开端会话隔离</span></div>'
      : '<div class="login-notes"><span><b>1</b>统一管理招聘流程</span><span><b>2</b>让候选人随时掌握进度</span><span><b>3</b>用数据辅助每一次决策</span></div>';
    var roleTabs = PORTAL === "admin" ? "" : '<div class="role-tabs">' +
      ["candidate", "hr"].map(function (item) {
        return '<button class="' + (role === item ? "active" : "") + '" data-role="' + item + '">' + roleLabel(item) + "</button>";
      }).join("") + '</div>';
    var formTitle = PORTAL === "admin" ? "管理员登录" : "欢迎回来";
    var formDesc = PORTAL === "admin" ? "请输入管理员账号和验证码进入内网" : "选择你的身份，进入 Recruit 工作台";
    return '<div class="login-page"><div class="login-frame"><section class="login-intro"><div class="brand"><span class="brand-mark">R</span><span><strong>Recruit</strong><small>招聘管理系统</small></span></div><h1>' + introTitle + '</h1><p>' + introText + '</p>' + introNotes + '</section><section class="login-form"><h2>' + formTitle + '</h2><p>' + formDesc + '</p>' + roleTabs + '<form id="login-form" class="login-fields"><div class="form-field"><label>' + accountLabel + '</label><input id="login-account" value="' + accountValue + '" autocomplete="username"></div><div class="form-field"><label>验证码</label><div class="code-row"><input id="login-code" placeholder="请输入 6 位验证码" inputmode="numeric" maxlength="6"><button type="button" class="button secondary" data-action="send-code">获取验证码</button></div></div><button class="button login-submit" type="submit">进入工作台　→</button></form><div class="demo-note">验证码由后端接口生成并写入 Redis；本地联调默认返回 123456，便于直接登录。</div></section></div></div>';
  }

  function dashboardView() {
    var pending = jobs.filter(function (job) { return job.status === 0; }).length;
    var active = jobs.filter(function (job) { return job.status === 1; }).length;
    var deliveryTotal = dashboard.deliveries || applications.length;
    var interviewTotal = dashboard.interviews || interviews.length;
    var latestDelivery = applications[0] || {};
    var latestInterview = interviews[0] || {};
    var latestReview = jobs.filter(function (job) { return job.status === 0; })[0] || {};
    return '<div class="page-head"><div><h1>招聘概览</h1><p>今天也为团队找到合适的人选。</p></div><div class="page-head-actions"><button class="button secondary" data-route="/hr/applications">查看新投递</button><button class="button" data-action="open-job">＋ 发布职位</button></div></div><div class="stats-grid">' +
      statCard("在招职位", active, "↗", "实时统计") + statCard("待处理投递", deliveryTotal, "◌", "接口同步") + statCard("本周面试", interviewTotal, "◷", "接口同步") + statCard("待审核职位", pending, "◇", "需要关注") +
      '</div><div class="dashboard-grid"><section class="panel"><div class="panel-head"><h2>职位动态</h2><button class="button ghost" data-route="/hr/jobs">查看全部 →</button></div><div class="panel-body"><div class="table-wrap">' + jobsTable(jobs.slice(0, 5), true) + '</div></div></section><section class="panel"><div class="panel-head"><h2>最近动态</h2><span>接口实时</span></div><div class="panel-body"><div class="timeline">' +
      '<div class="timeline-item"><i class="timeline-dot"></i><h3>新的简历投递</h3><p>「' + esc(latestDelivery.job || "暂无职位") + '」收到新的投递<br>' + esc(latestDelivery.time || "暂无") + '</p></div><div class="timeline-item"><i class="timeline-dot"></i><h3>面试安排</h3><p>「' + esc(latestInterview.job || "暂无职位") + '」面试状态更新<br>' + esc(latestInterview.time || "暂无") + '</p></div><div class="timeline-item"><i class="timeline-dot"></i><h3>职位审核</h3><p>「' + esc(latestReview.title || "暂无职位") + '」正在等待审核<br>' + esc(latestReview.createdAt || "暂无") + '</p></div></div></div></section></div>';
  }

  function statCard(title, value, icon, trend) {
    return '<article class="stat-card"><div class="stat-top"><span>' + title + '</span><span class="stat-icon">' + icon + '</span></div><div class="stat-value">' + value + '<span class="stat-trend">' + trend + "</span></div></article>";
  }

  function jobsTable(list, showActions, actionMode) {
    if (!list.length) return '<div class="empty">暂无职位记录</div>';
    var hasActions = showActions || actionMode;
    return '<table><thead><tr><th>职位</th><th>城市 / 薪资</th><th>投递</th><th>状态</th>' + (hasActions ? "<th>操作</th>" : "") + '</tr></thead><tbody>' +
      list.map(function (job) {
        var actions = "";
        if (actionMode === "admin-review") {
          actions = '<button class="action-link" data-action="job-detail" data-id="' + job.id + '">查看详情</button><button class="button small" data-action="approve-job" data-id="' + job.id + '">审核通过</button><button class="action-link danger" data-action="reject-job" data-id="' + job.id + '">拒绝</button>';
        } else if (actionMode === "admin-manage") {
          actions = '<button class="action-link" data-action="job-detail" data-id="' + job.id + '">查看详情</button>';
        } else if (showActions) {
          actions = '<button class="action-link" data-action="job-detail" data-id="' + job.id + '">查看</button><button class="action-link" data-action="edit-job" data-id="' + job.id + '">编辑</button>';
        }
        return '<tr><td><div class="job-title"><span class="company-mark">' + esc(initials(job.company)) + '</span><span><strong>' + esc(job.title) + '</strong><small>' + esc(job.company) + '</small></span></div></td><td>' + esc(job.city) + '　' + formatSalary(job) + '</td><td>' + job.deliveryCount + ' 人</td><td>' + statusTag(job.status, jobStatus) + '</td>' +
          (hasActions ? '<td>' + actions + '</td>' : "") + "</tr>";
      }).join("") + "</tbody></table>";
  }

  function adminPagination() {
    var totalPages = Math.max(1, Math.ceil((state.total || 0) / state.pageSize));
    return '<div class="panel-foot"><span>第 ' + state.page + ' / ' + totalPages + ' 页，共 ' + (state.total || 0) + ' 个岗位</span><div class="page-actions">' +
      '<button class="button ghost" data-action="admin-prev" ' + (state.page <= 1 ? "disabled" : "") + '>上一页</button>' +
      '<button class="button ghost" data-action="admin-next" ' + (state.page >= totalPages ? "disabled" : "") + '>下一页</button></div></div>';
  }

  function adminJobManageView() {
    return '<div class="page-head"><div><h1>职位管理</h1><p>查看平台内全部岗位及其当前状态。</p></div></div><div class="toolbar"><div class="search-box"><span class="search-symbol">⌕</span><input id="job-search" value="' + esc(state.search) + '" placeholder="搜索职位名称、企业或类别"></div><select id="city-filter" class="filter-select"><option value="">全部城市</option>' + ["北京", "上海", "深圳"].map(function (city) { return '<option ' + (state.city === city ? "selected" : "") + ">" + city + "</option>"; }).join("") + '</select><button class="button" data-action="search-admin-jobs">查询</button></div><section class="panel"><div class="panel-head"><h2>全部岗位</h2><span>管理员只读查看</span></div><div class="panel-body"><div class="table-wrap">' + jobsTable(jobs, false, "admin-manage") + '</div></div>' + adminPagination() + '</section>';
  }

  function jobManageView() {
    var list = jobs.filter(function (job) {
      var text = (job.title + job.company + job.category).toLowerCase();
      return (!state.search || text.indexOf(state.search.toLowerCase()) > -1) &&
        (!state.city || job.city === state.city) &&
        (state.status === "" || String(job.status) === state.status);
    });
    return '<div class="page-head"><div><h1>职位管理</h1><p>发布、维护和跟进你负责的每一个职位。</p></div><div class="page-head-actions"><button class="button" data-action="open-job">＋ 发布职位</button></div></div><div class="toolbar"><div class="search-box"><span class="search-symbol">⌕</span><input id="job-search" value="' + esc(state.search) + '" placeholder="搜索职位名称、企业或类别"></div><select id="city-filter" class="filter-select"><option value="">全部城市</option>' + ["北京", "上海", "深圳"].map(function (city) { return '<option ' + (state.city === city ? "selected" : "") + ">" + city + "</option>"; }).join("") + '</select><select id="status-filter" class="filter-select"><option value="">全部状态</option><option value="1" ' + (state.status === "1" ? "selected" : "") + '>招聘中</option><option value="0" ' + (state.status === "0" ? "selected" : "") + '>待审核</option><option value="3" ' + (state.status === "3" ? "selected" : "") + '>已下架</option></select></div><section class="panel"><div class="panel-head"><h2>职位列表</h2><span>共 ' + list.length + " 个职位</span></div><div class=\"panel-body\"><div class=\"table-wrap\">" + jobsTable(list, true) + "</div></div></section>";
  }

  function discoverView() {
    var list = jobs.filter(function (job) {
      var text = (job.title + job.company + job.category).toLowerCase();
      return job.status === 1 && (!state.search || text.indexOf(state.search.toLowerCase()) > -1) && (!state.city || job.city === state.city);
    });
    var hotSection = hotJobs.length ? '<section class="panel hot-jobs-panel"><div class="panel-head"><h2>热门岗位</h2><span>根据投递人数和浏览量排序</span></div><div class="job-grid">' + hotJobs.slice(0, 6).map(jobCard).join("") + '</div></section>' : "";
    return '<div class="page-head"><div><h1>职位发现</h1><p>找到值得投入时间的下一份工作。</p></div><div class="page-head-actions"><button class="button secondary" data-route="/candidate/deliveries">查看我的投递</button></div></div>' + hotSection + '<div class="toolbar"><div class="search-box"><span class="search-symbol">⌕</span><input id="job-search" value="' + esc(state.searchDraft !== null ? state.searchDraft : state.search) + '" placeholder="职位名称、技能或企业"></div><select id="city-filter" class="filter-select"><option value="">工作城市</option>' + ["北京", "上海", "深圳"].map(function (city) { return '<option ' + ((state.cityDraft !== null ? state.cityDraft : state.city) === city ? "selected" : "") + ">" + city + "</option>"; }).join("") + '</select><button class="button" data-action="search-jobs">搜索职位</button></div><div class="job-grid">' +
      (list.length ? list.map(jobCard).join("") : '<div class="panel empty">没有找到匹配职位，换个关键词试试。</div>') + "</div>";
  }

  function jobCard(job) {
    return '<article class="job-card"><div class="job-card-head"><div><h3>' + esc(job.title) + '</h3><div class="company">' + esc(job.company) + '</div></div><span class="company-mark">' + esc(initials(job.company)) + '</span></div><div class="salary">' + formatSalary(job) + '<small>/月</small></div><div class="job-meta"><span class="tag">' + esc(job.city) + '</span><span class="tag">' + esc(job.experience) + '</span><span class="tag">' + esc(job.education) + '</span></div><div class="job-card-foot"><span>' + job.deliveryCount + ' 人已投递</span><button data-action="job-detail" data-id="' + job.id + '">查看详情　→</button></div></article>';
  }

  function jobDetailView() {
    var job = jobById(state.jobId);
    if (!job) return '<div class="empty">职位不存在</div>';
    var candidate = state.role === "candidate";
    return '<div class="page-head"><div><button class="button ghost" data-action="back">← 返回列表</button></div></div><div class="detail-layout"><article class="detail-hero"><div class="job-card-head"><div><h1>' + esc(job.title) + '</h1><div class="detail-company">' + esc(job.company) + '　·　' + esc(job.city) + '</div></div><span class="company-mark">' + esc(initials(job.company)) + '</span></div><div class="detail-salary">' + formatSalary(job) + '<small>/月</small></div><div class="job-meta"><span class="tag">' + esc(job.category) + '</span><span class="tag">' + esc(job.experience) + '</span><span class="tag">' + esc(job.education) + '</span></div><div class="detail-section"><h2>职位描述</h2><p>' + esc(job.description) + '</p></div><div class="detail-section"><h2>任职要求</h2><p>' + esc(job.requirement) + '</p></div><div class="detail-section"><h2>福利待遇</h2><p>' + esc(String(job.benefits || "").replace(/,/g, "　·　")) + '</p></div></article><aside class="detail-side"><section class="side-card"><h2>职位概览</h2><div class="side-list"><div class="side-row"><span>当前投递</span><strong>' + job.deliveryCount + ' 人</strong></div><div class="side-row"><span>浏览次数</span><strong>' + job.viewCount + ' 次</strong></div><div class="side-row"><span>发布时间</span><strong>' + job.createdAt + '</strong></div></div>' + (candidate ? '<button class="button" style="width:100%;margin-top:21px" data-action="apply-job" data-id="' + job.id + '">选择简历并投递</button>' : "") + '</section><section class="side-card"><h2>企业信息</h2><div class="side-list"><div class="side-row"><span>所在行业</span><strong>' + esc(job.industry || job.category || "") + '</strong></div><div class="side-row"><span>企业规模</span><strong>' + esc(job.companySize || "") + '</strong></div><div class="side-row"><span>工作地点</span><strong>' + esc(job.address || job.city) + '</strong></div></div></section></aside></div>';
  }

  function applicationsView(candidateMode) {
    var list = applications;
    return '<div class="page-head"><div><h1>' + (candidateMode ? "我的投递" : "投递管理") + '</h1><p>' + (candidateMode ? "每一份投递，都值得被认真对待。" : "集中查看候选人资料，推进每一次沟通。") + '</p></div></div><div class="stats-grid">' + statCard("全部投递", list.length, "☷", candidateMode ? "持续更新" : "本周新增 3") + statCard("已查看", list.filter(function (x) { return x.status >= 1; }).length, "✓", "及时跟进") + statCard("面试阶段", list.filter(function (x) { return x.status === 2 || x.status === 3; }).length, "◷", "安排中") + statCard("Offer", list.filter(function (x) { return x.status >= 4 && x.status < 6; }).length, "◇", "重点关注") + '</div><section class="panel"><div class="panel-head"><h2>投递记录</h2><span>状态流转清晰可见</span></div><div class="panel-body"><div class="table-wrap"><table><thead><tr><th>候选人</th><th>应聘职位</th><th>简历</th><th>投递时间</th><th>当前状态</th><th>操作</th></tr></thead><tbody>' +
      list.map(function (item) { var job = jobById(item.jobId); return '<tr><td><div class="job-title"><span class="avatar">' + initials(item.candidate) + '</span><span><strong>' + item.candidate + '</strong><small>' + (candidateMode ? "求职者" : "候选人") + '</small></span></div></td><td>' + esc(job ? job.title : item.job) + '</td><td>' + esc(item.resume) + '</td><td>' + item.time + '</td><td>' + statusTag(item.status, applicationStatus) + '</td><td><button class="action-link" data-action="delivery-detail" data-id="' + item.id + '">查看进度</button>' + (!candidateMode ? '<button class="action-link" data-action="advance-delivery" data-id="' + item.id + '">推进</button>' : "") + "</td></tr>"; }).join("") + "</tbody></table></div></div></section>";
  }

  function notificationsView() {
    return '<div class="page-head"><div><h1>消息中心</h1><p>重要的进度变化，会在这里留下记录。</p></div><button class="button secondary" data-action="read-all">全部标记已读</button></div><section class="panel"><div class="panel-head"><h2>全部消息</h2><span>' + notifications.filter(function (x) { return x.unread; }).length + ' 条未读</span></div><div class="panel-body"><div class="notification-list">' + notifications.map(function (item) {
      return '<article class="notification ' + (item.unread ? "unread" : "") + '"><span class="notification-icon">●</span><div><h3>' + esc(item.title) + '</h3><p>' + esc(item.content) + '</p><time>' + item.time + '</time></div></article>';
    }).join("") + "</div></div></section>";
  }

  function companyView() {
    company = company || {};
    return '<div class="page-head"><div><h1>企业资料</h1><p>让候选人了解你们正在创造什么。</p></div><button class="button" data-action="save-company">保存修改</button></div><section class="panel"><div class="panel-body"><div class="form-grid"><div class="form-field"><label>企业名称</label><input id="company-name" value="' + esc(company.companyName || "") + '"></div><div class="form-field"><label>企业简称</label><input id="company-short-name" value="' + esc(company.shortName || "") + '"></div><div class="form-field"><label>所属行业</label><input id="company-industry" value="' + esc(company.industry || "") + '"></div><div class="form-field"><label>公司规模</label><input id="company-size" value="' + esc(company.companySize || "") + '"></div><div class="form-field full"><label>办公地址</label><input id="company-address" value="' + esc(company.address || "") + '"></div><div class="form-field full"><label>企业官网</label><input id="company-website" value="' + esc(company.website || "") + '"></div><div class="form-field full"><label>企业简介</label><textarea id="company-description">' + esc(company.description || "") + '</textarea></div></div></div></section>';
  }

  function adminDashboardView() {
    return '<div class="page-head"><div><h1>平台数据概览</h1><p>把招聘平台的关键变化放在一张桌面上。</p></div></div><div class="stats-grid">' + statCard("平台职位", dashboard.totalJobs || state.platformJobTotal || 0, "▦", "全部职位") + statCard("待审核", dashboard.pendingJobs || 0, "◇", "接口统计") + statCard("注册用户", dashboard.users || users.length, "◎", "HR " + (dashboard.hrs || 0) + " / 求职者 " + (dashboard.candidates || 0)) + statCard("累计投递", dashboard.deliveries || 0, "☷", "实时累计") + '</div><div class="dashboard-grid"><section class="panel"><div class="panel-head"><h2>待审核职位</h2><button class="button ghost" data-route="/admin/review">进入审核 →</button></div><div class="panel-body"><div class="table-wrap">' + jobsTable(pendingJobRecords, false) + '</div></div></section><section class="panel"><div class="panel-head"><h2>审核提示</h2><span>平台治理</span></div><div class="panel-body"><div class="timeline"><div class="timeline-item"><i class="timeline-dot"></i><h3>职位内容检查</h3><p>优先核验职位描述、薪资范围和企业信息是否完整。</p></div><div class="timeline-item"><i class="timeline-dot"></i><h3>处理时效</h3><p>建议在 1 个工作日内完成新职位审核。</p></div></div></div></section></div>';
  }

  function reviewView() {
    return '<div class="page-head"><div><h1>职位审核</h1><p>确认职位信息真实、完整、适合公开展示。</p></div></div><section class="panel"><div class="panel-head"><h2>待处理职位</h2><span>' + (state.total || 0) + ' 个待审核</span></div><div class="panel-body"><div class="table-wrap">' + jobsTable(jobs, false, "admin-review") + '</div></div>' + adminPagination() + '</section>';
  }

  function usersView() {
    return '<div class="page-head"><div><h1>用户管理</h1><p>查看平台角色和账户状态。</p></div></div><section class="panel"><div class="panel-head"><h2>用户列表</h2><span>共 ' + users.length + ' 个账户</span></div><div class="panel-body"><table><thead><tr><th>用户</th><th>角色</th><th>状态</th><th>操作</th></tr></thead><tbody>' + users.map(function (user) {
      var action = user.status === 1
        ? '<button class="action-link danger" data-action="toggle-user-status" data-id="' + user.id + '" data-status="0">禁用</button>'
        : user.status === 0
          ? '<button class="action-link" data-action="toggle-user-status" data-id="' + user.id + '" data-status="1">启用</button>'
          : '';
      var stateClass = user.status === 1 ? "success" : user.status === 2 ? "offline" : "pending";
      return '<tr><td><div class="job-title"><span class="avatar">' + initials(user.label) + '</span><span><strong>' + esc(user.label) + '</strong><small>' + esc(user.name) + '</small></span></div></td><td>' + esc(user.role) + '</td><td><span class="tag ' + stateClass + '">' + user.state + '</span></td><td>' + action + '</td></tr>';
    }).join("") + "</tbody></table></div></section>";
  }

  async function render() {
    var app = document.getElementById("app");
    normalizePortalRoute();
    var jobMatch = state.route.match(/\/jobs\/(\d+)/);
    state.jobId = jobMatch ? Number(jobMatch[1]) : null;
    if (PORTAL === "admin" && state.role && state.role !== "admin") {
      clearSession();
      state.role = "admin";
      app.innerHTML = loginView();
      return;
    }
    if (PORTAL !== "admin" && state.role === "admin") {
      clearSession();
      app.innerHTML = loginView();
      return;
    }
    if (!state.role || !state.token || state.route === "/login") {
      app.innerHTML = loginView();
      return;
    }

    try {
      app.innerHTML = shell('<div class="empty">正在从接口加载数据...</div>');
      await loadRoleData();
    } catch (error) {
      if (String(error.message).indexOf("401") > -1) {
        clearSession();
        app.innerHTML = loginView();
        return;
      }
      app.innerHTML = shell('<div class="empty">接口加载失败：' + esc(error.message) + '</div>');
      return;
    }

    if (state.route === "/hr/dashboard" || state.route === "/admin/dashboard" || state.route === "/") {
      app.innerHTML = shell(state.role === "admin" ? adminDashboardView() : state.role === "candidate" ? discoverView() : dashboardView());
      return;
    }
    var content;
    if (state.route === "/hr/jobs") content = jobManageView();
    else if (state.route.indexOf("/hr/jobs/") === 0 || state.route.indexOf("/candidate/jobs/") === 0) content = jobDetailView();
    else if (state.route === "/hr/applications") content = applicationsView(false);
    else if (state.route === "/candidate/discover") content = discoverView();
    else if (state.route === "/candidate/deliveries") content = applicationsView(true);
    else if (state.route === "/hr/interviews" || state.route === "/candidate/interviews") content = interviewsView();
    else if (state.route === "/candidate/resumes") content = resumesView();
    else if (state.route === "/hr/company") content = companyView();
    else if (state.route === "/hr/notifications" || state.route === "/candidate/notifications") content = notificationsView();
    else if (state.role === "admin" && state.route === "/admin/dashboard") content = adminDashboardView();
    else if (state.role === "admin" && state.route === "/admin/jobs") content = adminJobManageView();
    else if (state.role === "admin" && state.route === "/admin/review") content = reviewView();
    else if (state.role === "admin" && state.route === "/admin/users") content = usersView();
    else content = state.role === "candidate" ? discoverView() : dashboardView();
    app.innerHTML = shell(content);
  }

  function openJobModal(job) {
    var editing = !!job;
    state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>' + (editing ? "编辑职位" : "发布职位") + '</h2><button class="modal-close" data-action="close-modal">×</button></div><form id="job-form" class="modal-body"><input type="hidden" id="job-id" value="' + (editing ? job.id : "") + '"><div class="form-grid"><div class="form-field full"><label>职位名称</label><input id="job-title" required value="' + (editing ? esc(job.title) : "") + '" placeholder="如：Java 后端开发工程师"></div><div class="form-field"><label>职位类别</label><select id="job-category">' + optionList(["技术", "产品", "设计", "运营", "金融"], editing ? job.category : "技术") + '</select></div><div class="form-field"><label>工作城市</label><select id="job-city">' + optionList(["北京", "上海", "深圳"], editing ? job.city : "北京") + '</select></div><div class="form-field"><label>最低薪资（K/月）</label><input id="salary-min" type="number" value="' + (editing ? job.salaryMin : 15) + '"></div><div class="form-field"><label>最高薪资（K/月）</label><input id="salary-max" type="number" value="' + (editing ? job.salaryMax : 25) + '"></div><div class="form-field"><label>经验要求</label><select id="job-experience">' + optionList(["应届生", "1-3年", "3-5年", "5-10年"], editing ? job.experience : "3-5年") + '</select></div><div class="form-field"><label>学历要求</label><select id="job-education">' + optionList(["大专", "本科", "硕士", "博士"], editing ? job.education : "本科") + '</select></div><div class="form-field full"><label>职位描述</label><textarea id="job-description">' + (editing ? esc(job.description) : "") + '</textarea></div><div class="form-field full"><label>任职要求</label><textarea id="job-requirement">' + (editing ? esc(job.requirement) : "") + '</textarea></div><div class="form-field full"><label>福利待遇</label><input id="job-benefits" value="' + (editing ? esc(job.benefits) : "五险一金,双休") + '"></div></div></form><div class="modal-foot"><button class="button secondary" data-action="close-modal">取消</button><button class="button" data-action="save-job">' + (editing ? "保存修改" : "保存并提交") + '</button></div></section></div>';
    renderModal();
  }

  function renderModal() {
    var old = document.querySelector(".modal-backdrop");
    if (old) old.remove();
    if (state.modal) document.body.insertAdjacentHTML("beforeend", state.modal);
  }

  function openJobDetailModal(job) {
    if (!job) {
      alert("职位详情不存在");
      return;
    }
    state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>职位详情</h2><button class="modal-close" data-action="close-modal">×</button></div><div class="modal-body"><div class="detail-section"><h2>' + esc(job.title) + '</h2><p>' + esc(job.company) + ' · ' + esc(job.city) + ' · ' + formatSalary(job) + '</p></div><div class="form-grid"><div class="form-field"><label>职位类别</label><div>' + esc(job.category) + '</div></div><div class="form-field"><label>经验要求</label><div>' + esc(job.experience) + '</div></div><div class="form-field"><label>学历要求</label><div>' + esc(job.education) + '</div></div><div class="form-field"><label>当前状态</label><div>' + statusTag(job.status, jobStatus) + '</div></div><div class="form-field"><label>投递人数</label><div>' + job.deliveryCount + ' 人</div></div><div class="form-field"><label>浏览次数</label><div>' + job.viewCount + ' 次</div></div><div class="form-field full"><label>职位描述</label><div>' + esc(job.description || "暂无") + '</div></div><div class="form-field full"><label>任职要求</label><div>' + esc(job.requirement || "暂无") + '</div></div><div class="form-field full"><label>福利待遇</label><div>' + esc(job.benefits || "暂无") + '</div></div><div class="form-field full"><label>审核备注</label><div>' + esc(job.auditRemark || "暂无") + '</div></div></div></div></section></div>';
    renderModal();
  }

  async function openAdminJobDetail(id) {
    var job = jobById(id);
    try {
      job = normalizeJob(await apiFetch("/admin/jobs/" + id));
    } catch (error) {
      if (!job) throw error;
    }
    openJobDetailModal(job);
  }

  async function openHrJobDetail(id) {
    var job = jobById(id);
    try {
      job = normalizeJob(await apiFetch("/hr/jobs/" + id));
    } catch (error) {
      if (!job) throw error;
    }
    openJobDetailModal(job);
  }

  async function saveJob() {
    try {
      var id = Number(document.getElementById("job-id").value);
      var item = {
        id: id || undefined,
        companyId: company && company.id,
        title: document.getElementById("job-title").value || "未命名职位",
        category: document.getElementById("job-category").value,
        city: document.getElementById("job-city").value,
        salaryMin: Number(document.getElementById("salary-min").value || 0),
        salaryMax: Number(document.getElementById("salary-max").value || 0),
        workExperience: document.getElementById("job-experience").value,
        education: document.getElementById("job-education").value,
        jobDescription: document.getElementById("job-description").value,
        requirement: document.getElementById("job-requirement").value,
        benefits: document.getElementById("job-benefits").value
      };
      await apiFetch("/hr/jobs", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(item)
      });
      state.modal = null;
      invalidateData();
      render();
      alert("保存成功");
    } catch (error) {
      alert("保存失败，请稍后重试");
    }
  }

  async function showJobDetail(id) {
    state.jobId = Number(id);
    state.route = (state.role === "candidate" ? "/candidate/jobs/" : "/hr/jobs/") + id;
    state.currentJob = null;
    invalidateData();
    location.hash = state.route;
    render();
  }

  async function login() {
    var code = document.getElementById("login-code").value.trim();
    var account = document.getElementById("login-account").value.trim();
    if (!account || !/^\d{6}$/.test(code)) {
      alert("请输入账号和 6 位验证码");
      return;
    }
    var roleMap = PORTAL === "admin" ? { admin: 2 } : { candidate: 0, hr: 1 };
    state.role = PORTAL === "admin" ? "admin" : (state.role || "candidate");
    var loginResult = await apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ account: account, code: code, role: roleMap[state.role] })
    });
    state.userName = loginResult.username || account;
    state.token = loginResult.token;
    state.userId = loginResult.userId;
    writeStorage("role", state.role);
    writeStorage("user", state.userName);
    writeStorage("token", state.token);
    writeStorage("user-id", state.userId);
    state.route = state.role === "hr" ? "/hr/dashboard" : state.role === "admin" ? "/admin/dashboard" : "/candidate/discover";
    location.hash = state.route;
    invalidateData();
    render();
  }

  function normalizeInterview(item) {
    if (!item) return null;
    return {
      id: item.id,
      deliveryId: item.deliveryId,
      job: item.jobTitle || "",
      company: item.companyName || "",
      time: formatDateTime(item.interviewTime),
      interviewTime: item.interviewTime || "",
      duration: item.duration || 60,
      location: item.location || "",
      addressDetail: item.addressDetail || "",
      contactPerson: item.contactPerson || "",
      contactPhone: item.contactPhone || "",
      remark: item.remark || "",
      feedback: item.feedback || "",
      status: item.status,
      candidate: item.candidateName || "",
      interviewer: item.interviewer || ""
    };
  }

  function interviewStatusTag(status) {
    var map = {
      "0": ["待确认", "pending"],
      "1": ["已确认", "approved"],
      "2": ["已拒绝/已取消", "rejected"],
      "3": ["已完成", "success"]
    };
    return statusTag(status, map);
  }

  function interviewInputValue(value) {
    return value ? String(value).replace("T", " ").slice(0, 16).replace(" ", "T") : "";
  }

  function openInterviewModal(item) {
    var editing = !!item.interviewTime;
    var deliveryId = editing ? item.deliveryId : item.id;
    var interviewId = editing ? item.id : "";
    var title = editing ? "修改面试邀约" : "创建面试邀约";
    state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>' + title + '</h2><button class="modal-close" data-action="close-modal">×</button></div><form id="interview-form" class="modal-body"><input type="hidden" id="interview-id" value="' + interviewId + '"><input type="hidden" id="interview-delivery-id" value="' + deliveryId + '"><div class="form-grid"><div class="form-field"><label>面试官</label><input id="interviewer" value="' + esc(editing ? item.interviewer : "") + '"></div><div class="form-field"><label>面试时间</label><input id="interview-time" type="datetime-local" required value="' + interviewInputValue(editing ? item.interviewTime : "") + '"></div><div class="form-field"><label>面试时长（分钟）</label><input id="interview-duration" type="number" min="15" value="' + (editing ? item.duration : 60) + '"></div><div class="form-field"><label>面试地点</label><input id="interview-location" required value="' + esc(editing ? item.location : "") + '"></div><div class="form-field"><label>联系人</label><input id="contact-person" value="' + esc(editing ? item.contactPerson : "") + '"></div><div class="form-field"><label>联系电话</label><input id="contact-phone" value="' + esc(editing ? item.contactPhone : "") + '"></div><div class="form-field full"><label>详细地址</label><input id="address-detail" value="' + esc(editing ? item.addressDetail : "") + '"></div><div class="form-field full"><label>备注</label><textarea id="interview-remark">' + esc(editing ? item.remark : "") + '</textarea></div></div></form><div class="modal-foot"><button class="button secondary" data-action="close-modal">取消</button><button class="button" data-action="save-interview">保存邀约</button></div></section></div>';
    renderModal();
  }

  async function saveInterview() {
    var id = Number(document.getElementById("interview-id").value || 0);
    var payload = {
      deliveryId: Number(document.getElementById("interview-delivery-id").value),
      interviewer: document.getElementById("interviewer").value,
      interviewTime: document.getElementById("interview-time").value,
      duration: Number(document.getElementById("interview-duration").value || 60),
      location: document.getElementById("interview-location").value,
      addressDetail: document.getElementById("address-detail").value,
      contactPerson: document.getElementById("contact-person").value,
      contactPhone: document.getElementById("contact-phone").value,
      remark: document.getElementById("interview-remark").value
    };
    await apiFetch(id ? "/hr/interviews/" + id : "/hr/interviews", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    state.modal = null;
    renderModal();
    invalidateData();
    render();
    alert("面试邀约保存成功");
  }

  async function openDeliveryDetail(id) {
    var path = state.role === "hr" ? "/hr/applications/" + id : "/candidate/deliveries/" + id;
    var item = normalizeDelivery(await apiFetch(path));
    var terminal = item.status === 6 || item.status === 7;
    var timelineStatuses = terminal ? [item.status] : [0, 1, 2, 3, 4, 5];
    var terminalLabel = item.status === 6 && item.statusRemark.indexOf("面试不通过") >= 0
      ? ["面试不通过", "rejected"]
      : applicationStatus[String(item.status)];
    var timeline = timelineStatuses.map(function (status) {
      var isTerminal = status === 6 || status === 7;
      var tag = isTerminal && terminalLabel ? '<span class="tag ' + terminalLabel[1] + '">' + terminalLabel[0] + '</span>' : statusTag(status, applicationStatus);
      return '<div class="timeline-item' + (isTerminal ? ' terminal' : '') + '"><i class="timeline-dot"></i><h3>' + tag + '</h3><p>' + (isTerminal ? "流程已终止" : (item.status >= status ? "已完成" : "待推进")) + '</p></div>';
    }).join("");
    state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>投递进度</h2><button class="modal-close" data-action="close-modal">×</button></div><div class="modal-body"><div class="detail-section"><h2>' + esc(item.job) + '</h2><p>' + esc(item.company) + ' · ' + esc(item.candidate) + '</p></div><div class="form-grid"><div class="form-field"><label>简历</label><div>' + esc(item.resume) + '</div></div><div class="form-field"><label>投递时间</label><div>' + esc(item.time) + '</div></div><div class="form-field"><label>当前状态</label><div>' + statusTag(item.status, applicationStatus) + '</div></div><div class="form-field"><label>学历</label><div>' + esc(item.education) + '</div></div><div class="form-field full"><label>技能与经历</label><div>' + esc(item.skill || item.school || "暂无") + '</div></div><div class="form-field full"><label>状态备注</label><div>' + esc(item.statusRemark || "暂无") + '</div></div></div><div class="timeline">' + timeline + '</div></div></section></div>';
    renderModal();
  }

  function openResumeModal(resume) {
    var editing = !!resume;
    state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>' + (editing ? "编辑简历" : "新建简历") + '</h2><button class="modal-close" data-action="close-modal">×</button></div><form id="resume-form" class="modal-body"><input type="hidden" id="resume-id" value="' + (editing ? resume.id : '') + '"><div class="form-grid"><div class="form-field"><label>姓名</label><input id="resume-name" required value="' + esc(editing ? resume.name : '') + '"></div><div class="form-field"><label>性别</label><select id="resume-gender">' + optionList(["未知", "男", "女"], editing ? (resume.gender === 1 ? "男" : resume.gender === 2 ? "女" : "未知") : "未知") + '</select></div><div class="form-field"><label>出生日期</label><input id="resume-birth-date" type="date" value="' + esc(editing ? resume.birthDate : '') + '"></div><div class="form-field"><label>手机号</label><input id="resume-phone" required value="' + esc(editing ? resume.phone : '') + '"></div><div class="form-field"><label>邮箱</label><input id="resume-email" type="email" value="' + esc(editing ? resume.email : '') + '"></div><div class="form-field"><label>最高学历</label><select id="resume-education">' + optionList(["大专", "本科", "硕士", "博士"], editing ? resume.education : "本科") + '</select></div><div class="form-field"><label>毕业院校</label><input id="resume-school" value="' + esc(editing ? resume.school : '') + '"></div><div class="form-field"><label>专业</label><input id="resume-major" value="' + esc(editing ? resume.major : '') + '"></div><div class="form-field full"><label>现居地址</label><input id="resume-address" value="' + esc(editing ? resume.address : '') + '"></div><div class="form-field full"><label>工作经历</label><textarea id="resume-work-experience">' + esc(editing ? resume.workExperience : '') + '</textarea></div><div class="form-field full"><label>项目经验</label><textarea id="resume-project-experience">' + esc(editing ? resume.projectExperience : '') + '</textarea></div><div class="form-field full"><label>技能标签</label><input id="resume-skill" value="' + esc(editing ? resume.skills : '') + '" placeholder="例如：Java、MySQL、Redis"></div><div class="form-field full"><label>自我评价</label><textarea id="resume-self-evaluation">' + esc(editing ? resume.selfEvaluation : '') + '</textarea></div><div class="form-field full"><label class="checkbox-field"><input id="resume-default" type="checkbox" ' + (editing && resume.default ? 'checked' : '') + '>设为默认简历</label></div></div></form><div class="modal-foot"><button class="button secondary" data-action="close-modal">取消</button><button class="button" data-action="save-resume">保存简历</button></div></section></div>';
    renderModal();
  }

  async function saveResume() {
    var id = Number(document.getElementById("resume-id").value || 0);
    var genderText = document.getElementById("resume-gender").value;
    var payload = {
      name: document.getElementById("resume-name").value,
      gender: genderText === "男" ? 1 : genderText === "女" ? 2 : 0,
      birthDate: document.getElementById("resume-birth-date").value || null,
      phone: document.getElementById("resume-phone").value,
      email: document.getElementById("resume-email").value,
      address: document.getElementById("resume-address").value,
      education: document.getElementById("resume-education").value,
      school: document.getElementById("resume-school").value,
      major: document.getElementById("resume-major").value,
      workExperience: document.getElementById("resume-work-experience").value,
      projectExperience: document.getElementById("resume-project-experience").value,
      skill: document.getElementById("resume-skill").value,
      selfEvaluation: document.getElementById("resume-self-evaluation").value,
      isDefault: document.getElementById("resume-default").checked ? 1 : 0
    };
    await apiFetch(id ? "/candidate/resumes/" + id : "/candidate/resumes", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    state.modal = null;
    renderModal();
    invalidateData();
    render();
    alert("简历保存成功");
  }

  function resumesView() {
    return '<div class="page-head"><div><h1>我的简历</h1><p>维护不同方向的简历，让经历被准确看见。</p></div><button class="button" data-action="new-resume">＋ 新建简历</button></div><div class="resume-grid">' +
      (resumes.length ? resumes.map(function (resume) {
        return '<article class="resume-card"><div class="resume-card-head"><div><h3>' + esc(resume.name) + '</h3><p>' + esc([resume.education, resume.school].filter(Boolean).join(" · ")) + '</p></div>' + (resume.default ? '<span class="tag approved">默认</span>' : "") + '</div><p>' + esc(resume.summary || resume.selfEvaluation || "暂无经历描述") + '</p><div class="skill-line">' + esc(resume.skills || "暂无技能标签") + '</div><div class="job-card-foot"><span>更新于 ' + resume.updated + '</span><span class="inline-actions"><button class="action-link" data-action="edit-resume" data-id="' + resume.id + '">编辑</button>' + (!resume.default ? '<button class="action-link" data-action="default-resume" data-id="' + resume.id + '">设为默认</button>' : '') + '<button class="action-link danger" data-action="delete-resume" data-id="' + resume.id + '">删除</button></span></div></article>';
      }).join("") : '<div class="panel empty">还没有简历，请先新建一份简历。</div>') + "</div>";
  }

  function applicationsView(candidateMode) {
    var list = applications;
    return '<div class="page-head"><div><h1>' + (candidateMode ? "我的投递" : "投递管理") + '</h1><p>' + (candidateMode ? "每一份投递，都值得被认真对待。" : "集中查看候选人资料，推进每一次沟通。") + '</p></div></div><div class="stats-grid">' +
      statCard("全部投递", list.length, "☷", "接口同步") + statCard("已查看", list.filter(function (x) { return x.status >= 1; }).length, "✓", "及时跟进") + statCard("面试阶段", list.filter(function (x) { return x.status === 2 || x.status === 3; }).length, "◷", "安排中") + statCard("Offer", list.filter(function (x) { return x.status >= 4 && x.status < 6; }).length, "◇", "重点关注") +
      '</div><section class="panel"><div class="panel-head"><h2>投递记录</h2><span>状态流转清晰可见</span></div><div class="panel-body"><div class="table-wrap"><table><thead><tr><th>' + (candidateMode ? "求职者" : "候选人") + '</th><th>应聘职位</th><th>简历</th><th>投递时间</th><th>当前状态</th><th>操作</th></tr></thead><tbody>' +
      list.map(function (item) {
        var job = jobById(item.jobId);
        var action = "";
        if (!candidateMode) {
          if (item.status === 0) action = '<button class="action-link" data-action="advance-delivery" data-id="' + item.id + '">标记已查看</button>';
          else if (item.status === 1) action = '<button class="action-link" data-action="open-interview" data-id="' + item.id + '">创建面试</button><button class="action-link danger" data-action="reject-delivery" data-id="' + item.id + '">不合适</button>';
          else if (item.status === 2) {
            var interview = interviews.find(function (record) {
              return record.deliveryId === item.id;
            });
            if (interview && interview.status !== 2 && interview.status !== 4 && interview.status !== 3) {
              action = '<button class="action-link" data-action="pass-interview" data-id="' + interview.id + '">面试通过</button><button class="action-link danger" data-action="fail-interview" data-id="' + interview.id + '">面试不通过</button>';
            }
          }
          else if (item.status >= 3 && item.status < 5) action = '<button class="action-link" data-action="advance-delivery" data-id="' + item.id + '">推进</button>';
        }
        return '<tr><td><div class="job-title"><span class="avatar">' + initials(item.candidate) + '</span><span><strong>' + esc(item.candidate) + '</strong><small>' + (candidateMode ? "求职者" : "候选人") + '</small></span></div></td><td>' + esc(job ? job.title : item.job) + '</td><td>' + esc(item.resume) + '</td><td>' + item.time + '</td><td>' + statusTag(item.status, applicationStatus) + '</td><td><button class="action-link" data-action="delivery-detail" data-id="' + item.id + '">查看进度</button>' + action + '</td></tr>';
      }).join("") + "</tbody></table></div></div></section>";
  }

  function interviewsView() {
    var list = interviews;
    return '<div class="page-head"><div><h1>' + (state.role === "candidate" ? "面试邀约" : "面试安排") + '</h1><p>' + (state.role === "candidate" ? "确认时间，带着准备好的自己出发。" : "安排面试时间，保持候选人体验连贯。") + '</p></div></div><section class="panel"><div class="panel-head"><h2>面试日程</h2><span>共 ' + list.length + ' 场</span></div><div class="panel-body"><div class="timeline">' + list.map(function (item) {
      var actions = "";
      if (state.role === "candidate" && item.status === 0) {
        actions = '<button class="action-link" data-action="accept-interview" data-id="' + item.id + '">接受</button><button class="action-link danger" data-action="reject-interview" data-id="' + item.id + '">拒绝</button>';
      } else if (state.role === "hr") {
        if (item.status === 0 || item.status === 1) actions = '<button class="action-link" data-action="edit-interview" data-id="' + item.id + '">修改</button><button class="action-link danger" data-action="cancel-interview" data-id="' + item.id + '">取消</button>';
        if (item.status === 1) actions += '<button class="action-link" data-action="finish-interview" data-id="' + item.id + '">标记完成</button>';
      }
      return '<div class="timeline-item"><i class="timeline-dot"></i><h3>' + esc(item.job) + '　' + interviewStatusTag(item.status) + '</h3><p>' + esc(item.company) + '<br>' + esc(item.time) + ' · ' + esc(item.location) + '<br>' + (state.role === "candidate" ? "面试官：" + esc(item.interviewer) : "候选人：" + esc(item.candidate)) + '</p><div class="inline-actions">' + actions + '</div></div>';
    }).join("") + '</div></div></section>';
  }

  document.addEventListener("click", async function (event) {
    var target = event.target.closest("[data-action]");
    if (!target) return;
    var action = target.dataset.action;
    var handled = ["open-interview", "save-interview", "edit-interview", "cancel-interview",
      "finish-interview", "accept-interview", "reject-interview", "delivery-detail",
      "advance-delivery", "reject-delivery", "pass-interview", "fail-interview",
      "new-resume", "save-resume", "edit-resume", "delete-resume", "default-resume"].indexOf(action) >= 0;
    if (!handled) return;
    event.preventDefault();
    event.stopImmediatePropagation();
    try {
      if (action === "open-interview") {
        var delivery = applications.find(function (item) {
          return item.id === Number(target.dataset.id);
        });
        if (delivery) openInterviewModal(delivery);
      } else if (action === "save-interview") {
        await saveInterview();
      } else if (action === "edit-interview") {
        var interview = interviews.find(function (item) {
          return item.id === Number(target.dataset.id);
        });
        if (interview) openInterviewModal(interview);
      } else if (action === "cancel-interview") {
        var remark = window.prompt("请输入取消面试的原因：", "HR 已取消面试邀约");
        if (remark === null) return;
        await apiFetch("/hr/interviews/" + target.dataset.id + "?remark=" + encodeURIComponent(remark), {
          method: "DELETE"
        });
        invalidateData();
        render();
        alert("面试邀约已取消");
      } else if (action === "finish-interview") {
        await apiFetch("/hr/interviews/" + target.dataset.id + "/status", {
          method: "PUT",
          body: JSON.stringify({ status: 3, remark: "面试已完成" })
        });
        invalidateData();
        render();
      } else if (action === "reject-delivery") {
        await apiFetch("/hr/applications/" + target.dataset.id + "/status", {
          method: "PUT",
          body: JSON.stringify({ status: 6, remark: "HR 判断为不合适" })
        });
        invalidateData();
        render();
      } else if (action === "pass-interview" || action === "fail-interview") {
        await apiFetch("/hr/interviews/" + target.dataset.id + "/status", {
          method: "PUT",
          body: JSON.stringify({
            status: action === "pass-interview" ? 3 : 2,
            remark: action === "pass-interview" ? "面试通过" : "面试不通过"
          })
        });
        invalidateData();
        render();
      } else if (action === "new-resume") {
        openResumeModal(null);
      } else if (action === "save-resume") {
        await saveResume();
      } else if (action === "edit-resume") {
        var resume = resumes.find(function (item) {
          return item.id === Number(target.dataset.id);
        });
        if (resume) openResumeModal(resume);
      } else if (action === "delete-resume") {
        if (!window.confirm("确定删除这份简历吗？")) return;
        await apiFetch("/candidate/resumes/" + target.dataset.id, { method: "DELETE" });
        invalidateData();
        render();
        alert("简历已删除");
      } else if (action === "default-resume") {
        await apiFetch("/candidate/resumes/" + target.dataset.id + "/default", { method: "PUT" });
        invalidateData();
        render();
        alert("默认简历设置成功");
      } else if (action === "accept-interview" || action === "reject-interview") {
        await apiFetch("/candidate/interviews/" + target.dataset.id + "/status", {
          method: "PUT",
          body: JSON.stringify({
            status: action === "accept-interview" ? 1 : 2,
            remark: action === "accept-interview" ? "求职者已确认参加面试" : "求职者拒绝面试邀约"
          })
        });
        invalidateData();
        render();
      } else if (action === "delivery-detail") {
        await openDeliveryDetail(Number(target.dataset.id));
      } else if (action === "advance-delivery") {
        var application = applications.find(function (item) {
          return item.id === Number(target.dataset.id);
        });
        if (!application) return;
        if (application.status === 1) {
          openInterviewModal(application);
          return;
        }
        var nextStatus = application.status + 1;
        await apiFetch("/hr/applications/" + application.id + "/status", {
          method: "PUT",
          body: JSON.stringify({ status: nextStatus })
        });
        invalidateData();
        render();
      }
    } catch (error) {
      alert(error.message || "操作失败，请稍后重试");
    }
  }, true);

  document.addEventListener("click", async function (event) {
    var target = event.target.closest("[data-route],[data-action],[data-role]");
    if (!target) return;
    try {
      if (target.dataset.role) {
        state.role = target.dataset.role;
        render();
        return;
      }
      if (target.dataset.route) {
        state.route = target.dataset.route;
        state.jobId = null;
        state.currentJob = null;
        state.page = 1;
        invalidateData();
        location.hash = state.route;
        render();
        return;
      }
      var action = target.dataset.action;
      if (action === "logout") {
        clearSession();
        location.hash = "/login";
        render();
      } else if (action === "toggle-sidebar") document.getElementById("sidebar").classList.toggle("open");
      else if (action === "send-code") {
        var account = document.getElementById("login-account").value.trim();
        var roleMap = PORTAL === "admin" ? { admin: 2 } : { candidate: 0, hr: 1 };
        var code = await apiFetch("/auth/code", {
          method: "POST",
          body: JSON.stringify({ account: account, role: roleMap[PORTAL === "admin" ? "admin" : (state.role || "candidate")] })
        });
        document.getElementById("login-code").value = code || "123456";
        target.textContent = "已发送";
      }
      else if (action === "open-job") openJobModal();
      else if (action === "edit-job") openJobModal(jobById(target.dataset.id));
      else if (action === "close-modal") { state.modal = null; renderModal(); }
      else if (action === "save-job") await saveJob();
      else if (action === "job-detail") {
        if (state.role === "admin") await openAdminJobDetail(target.dataset.id);
        else if (state.role === "hr" && state.route === "/hr/jobs") await openHrJobDetail(target.dataset.id);
        else await showJobDetail(target.dataset.id);
      }
      else if (action === "admin-prev" && state.page > 1) {
        state.page -= 1;
        invalidateData();
        render();
      }
      else if (action === "admin-next") {
        var totalPages = Math.max(1, Math.ceil((state.total || 0) / state.pageSize));
        if (state.page < totalPages) {
          state.page += 1;
          invalidateData();
          render();
        }
      }
      else if (action === "back") { state.route = state.role === "candidate" ? "/candidate/discover" : "/hr/jobs"; state.currentJob = null; invalidateData(); location.hash = state.route; render(); }
      else if (action === "apply-job") state.modal = '<div class="modal-backdrop"><section class="modal"><div class="modal-head"><h2>选择投递简历</h2><button class="modal-close" data-action="close-modal">×</button></div><div class="modal-body"><p style="margin-top:0;color:var(--muted);font-size:13px">请选择用于申请该职位的简历。</p><div class="resume-grid">' + resumes.map(function (resume) { return '<article class="resume-card"><div class="resume-card-head"><h3>' + esc(resume.name) + '</h3>' + (resume.default ? '<span class="tag approved">默认</span>' : "") + '</div><p>' + esc(resume.education) + '<br>' + esc(resume.summary) + '</p><button class="button small" style="margin-top:14px" data-action="confirm-apply" data-id="' + resume.id + '">使用这份简历</button></article>'; }).join("") + "</div></div></section></div>", renderModal();
      else if (action === "confirm-apply") {
        await apiFetch("/candidate/deliveries", {
          method: "POST",
          body: JSON.stringify({ jobId: state.jobId, resumeId: Number(target.dataset.id) })
        });
        state.modal = null;
        invalidateData();
        renderModal();
        render();
        alert("投递成功，状态为“待沟通”");
      }
      else if (action === "advance-delivery") {
        var delivery = applications.find(function (x) { return x.id === Number(target.dataset.id); });
        if (!delivery || delivery.status >= 5) return;
        await apiFetch("/hr/applications/" + delivery.id + "/status", {
          method: "PUT",
          body: JSON.stringify({ status: delivery.status + 1 })
        });
        invalidateData();
        render();
      }
      else if (action === "approve-job" || action === "reject-job") {
        await apiFetch("/admin/reviews/" + target.dataset.id, {
          method: "POST",
          body: JSON.stringify({ status: action === "approve-job" ? 1 : 2, remark: action === "approve-job" ? "审核通过" : "信息不完整，请修改后重新提交" })
        });
        invalidateData();
        render();
      }
      else if (action === "search-jobs") {
        if (state.role === "candidate" && state.route === "/candidate/discover") {
          state.search = state.searchDraft || "";
          state.city = state.cityDraft || "";
          state.page = 1;
          invalidateData();
          render();
        } else {
          invalidateData();
          render();
        }
      }
      else if (action === "search-admin-jobs") {
        state.page = 1;
        invalidateData();
        render();
      }
      else if (action === "toggle-user-status") {
        var targetStatus = Number(target.dataset.status);
        if (targetStatus !== 0 && targetStatus !== 1) return;
        await apiFetch("/admin/users/" + target.dataset.id + "/status?status=" + targetStatus, {
          method: "PUT"
        });
        invalidateData();
        render();
      }
      else if (action === "read-all") {
        await Promise.all(notifications.filter(function (x) { return x.unread; }).map(function (item) {
          return apiFetch(roleRoute() + "/notifications/" + item.id + "/read", { method: "PUT" });
        }));
        invalidateData();
        render();
      }
      else if (action === "save-company") {
        try {
          await apiFetch("/hr/company", {
            method: "PUT",
            body: JSON.stringify({
              companyName: document.getElementById("company-name").value,
              shortName: document.getElementById("company-short-name").value,
              industry: document.getElementById("company-industry").value,
              companySize: document.getElementById("company-size").value,
              address: document.getElementById("company-address").value,
              website: document.getElementById("company-website").value,
              description: document.getElementById("company-description").value
            })
          });
          invalidateData();
          render();
          alert("保存成功");
        } catch (error) {
          alert("保存失败，请稍后重试");
        }
      }
    } catch (error) {
      alert(error.message || "操作失败");
    }
  });

  document.addEventListener("submit", function (event) {
    if (event.target.id === "login-form") {
      event.preventDefault();
      login().catch(function (error) {
        alert(error.message || "登录失败");
      });
    }
  });

  document.addEventListener("change", function (event) {
    if (event.target.id === "city-filter") {
      if (state.role === "candidate" && state.route === "/candidate/discover") {
        state.cityDraft = event.target.value;
        return;
      }
      state.city = event.target.value;
      state.page = 1;
      if (!(state.role === "admin" && state.route === "/admin/jobs")) {
        invalidateData();
        render();
      }
    }
    if (event.target.id === "status-filter") { state.status = event.target.value; state.page = 1; invalidateData(); render(); }
  });

  document.addEventListener("input", function (event) {
    if (event.target.id === "job-search") {
      if (state.role === "candidate" && state.route === "/candidate/discover") {
        state.searchDraft = event.target.value;
        return;
      }
      state.search = event.target.value;
      state.page = 1;
      if (!(state.role === "admin" && state.route === "/admin/jobs") &&
        (event.target.value === "" || event.inputType === "insertLineBreak")) {
        invalidateData();
        render();
      }
    }
  });

  window.addEventListener("hashchange", function () {
    state.route = location.hash.replace("#", "") || "/login";
    invalidateData();
    render();
  });

  render();
})();

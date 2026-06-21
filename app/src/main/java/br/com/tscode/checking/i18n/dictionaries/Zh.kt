package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun zhDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Checking 使用手册",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "签到和签退登记表单",
        "credentialsAria" to "用户标识和密码",
        "keyLabel" to "钥匙码",
        "passwordLabel" to "密码",
        "keyPlaceholder" to "例如：HR70",
        "passwordPlaceholder" to "3 到 10 个字符",
        "requestRegistrationButton" to "申请注册",
        "awaitingApproval" to "正在等待注册审批。",
        "registrationQueueFull" to "注册队列已满。请联系系统管理员。",
        "settingsSpacer" to "设置",
        "openSettingsAria" to "打开设置",
        "openSettingsTitle" to "打开设置",
        "waitingAuthentication" to "等待身份验证。",
        "enterPasswordPrompt" to "请输入密码以开始。",
        "createPasswordPrompt" to "请输入钥匙码并创建密码。",
        "invalidFourCharacterKey" to "请输入 4 位字母数字钥匙码。",
        "unknownUserDetail" to "该用户钥匙码未注册",
        "transportAccessPrompt" to "请输入钥匙码并验证密码以访问交通功能。",
    ),
    "history" to d(
        "lastCheckinLabel" to "最近一次签到",
        "lastCheckoutLabel" to "最近一次签退",
        "today" to "今天",
        "yesterday" to "昨天",
        "dialogTitleCheckin" to "签到历史",
        "dialogTitleCheckout" to "签退历史",
        "colDate" to "日期",
        "colTime" to "时间",
        "colLocal" to "地点",
        "empty" to "未找到记录。",
        "back" to "返回",
        "loadingMessage" to "正在查询历史记录...",
        "notFoundMessage" to "未找到此钥匙码的记录。",
        "noRecordsMessage" to "此钥匙码没有签到或签退记录。",
        "updatedMessage" to "已更新该钥匙码的历史记录。",
        "loadFailed" to "无法查询此钥匙码的历史记录。",
        "colActivity" to "活动",
        "activityCheckin" to "签到",
        "activityCheckout" to "签退",
        "loadError" to "无法加载历史记录。",
        "retry" to "重试",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "自动活动",
        "sectionTitle" to "登记",
        "checkinLabel" to "签到",
        "checkoutLabel" to "签退",
        "transportLabel" to "运输",
        "informeTitle" to "类型",
        "informeNormalLabel" to "正常",
        "informeRetroativoLabel" to "补录",
        "submitButton" to "登记",
        "checkInLowerLabel" to "签到",
        "checkOutLowerLabel" to "签退",
        "disableAutomaticActivitiesForManualSubmit" to "请先关闭自动活动后再手动登记。",
        "selectLocationBeforeSubmit" to "登记前请选择一个位置。",
    ),
    "settings" to d(
        "title" to "设置",
        "languageLabel" to "语言",
        "resetPasswordLabel" to "更改密码",
        "allowLocationLabel" to "允许定位",
        "allowAudioVideoLabel" to "允许音频和视频",
        "supportLabel" to "支持",
        "manualLabel" to "使用说明",
        "aboutLabel" to "关于",
        "activitiesLabel" to "活动",
        "backButton" to "返回",
    ),
    "passwordDialog" to d(
        "titleChange" to "更改密码",
        "titleRegister" to "创建密码",
        "oldPasswordLabel" to "旧密码",
        "newPasswordLabel" to "新密码",
        "confirmPasswordLabel" to "确认密码",
        "backButton" to "返回",
        "submitChangeButton" to "更改",
        "submitRegisterButton" to "保存",
        "changingStatus" to "正在更改密码...",
        "savingStatus" to "正在保存密码...",
        "validatingStatus" to "正在验证密码。",
        "oldPasswordInvalid" to "旧密码长度必须在 3 到 10 个字符之间。",
        "newPasswordInvalid" to "新密码长度必须在 3 到 10 个字符之间。",
        "confirmMismatch" to "新密码确认不一致。",
        "changeFailed" to "无法更改密码。",
        "validationFailed" to "无法验证密码。",
        "statusLoadFailed" to "无法检查密码状态。",
    ),
    "registrationDialog" to d(
        "title" to "申请注册",
        "note" to "请填写以下信息以使用 Checking 系统。",
        "keyLabel" to "钥匙码",
        "fullNameLabel" to "完整姓名",
        "projectsLabel" to "项目",
        "projectsHint" to "请选择一个或多个项目。",
        "emailLabel" to "电子邮箱",
        "emailPlaceholder" to "可选",
        "passwordLabel" to "密码",
        "confirmPasswordLabel" to "确认密码",
        "backButton" to "返回",
        "submitButton" to "发送",
        "loadingProjects" to "正在加载项目...",
        "noProjectsAvailable" to "当前没有可用项目。",
        "fullNameRequired" to "请输入完整姓名。",
        "emailInvalid" to "请输入有效邮箱，或将该字段留空。",
        "passwordInvalid" to "密码长度必须在 3 到 10 个字符之间。",
        "confirmMismatch" to "新密码确认不一致。",
        "submittingStatus" to "正在发送注册申请...",
        "successStatus" to "注册成功完成。",
        "submitFailed" to "无法发送注册申请。",
    ),
    "location" to d(
        "title" to "位置",
        "waitingLabel" to "正在等待定位。",
        "refreshLabel" to "刷新位置",
        "refreshBusyLabel" to "正在刷新位置",
        "unavailableShort" to "不可用",
        "unavailableLabel" to "位置不可用",
        "unavailableMessage" to "当前无法检查位置。",
        "noPermissionLabel" to "无权限",
        "timeoutLabel" to "已超时",
        "timeoutMessage" to "位置查询耗时超出预期。",
        "detectingLabel" to "正在检测...",
        "exactConfirmationBrowser" to "正在等待浏览器确认精确位置。",
        "exactConfirmationApp" to "正在等待快捷方式/应用确认精确位置。",
        "updatingDeviceLocation" to "正在更新设备当前位置。",
        "secureContextRequired" to "精确位置需要安全连接（HTTPS）。",
        "browserUnsupported" to "此浏览器不支持精确定位。",
        "permissionBlocked" to "浏览器已阻止定位权限。请在浏览器设置中允许该站点访问位置。",
        "captureRequiresSupport" to "位置捕获需要 HTTPS 和浏览器支持。",
        "noValidPosition" to "无法获取有效的设备位置。",
        "searchingPrecision" to "正在搜索足够精度...",
        "completionStatus" to "位置更新已完成。",
        "completionStatusWithDetail" to "位置更新已完成。{detail}",
        "browserContextLabel" to "在此浏览器中",
        "appContextLabel" to "在此快捷方式/应用中",
        "browserSourceLabel" to "通过浏览器",
        "appSourceLabel" to "通过快捷方式/应用",
        "currentAccuracyLabel" to "当前精度",
        "accuracyPrefix" to "精度",
        "accuracyTemplate" to "精度 {accuracy}",
        "accuracyLimitTemplate" to "限制 {limit} 米",
        "accuracyCombinedTemplate" to "精度 {accuracy} / 限制 {limit} 米",
        "noKnownLocations" to "没有已登记位置",
        "defaultManualLocationLabel" to "主办公室",
        "accuracyFallbackManualLocationLabel" to "精度不足",
        "outsideWorkplaceLabel" to "工作地点之外",
        "unregisteredLocationLabel" to "未登记位置",
        "mixedZoneLabel" to "混合区域",
        "checkoutZoneLabel" to "签退区域",
    ),
    "projects" to d(
        "label" to "项目",
        "changeButton" to "更改",
        "loadingProjects" to "正在加载项目...",
        "updatingProjects" to "正在更新项目...",
        "noneAvailableShort" to "没有可用项目",
        "noneAvailableSentence" to "没有可用项目。",
        "noneAvailableNow" to "当前没有可用项目。",
        "selectAtLeastOne" to "请至少选择一个项目。",
        "userProjectsAria" to "用户项目",
        "registrationProjectsAria" to "注册项目",
        "updatedSuccess" to "项目更新成功。",
        "loadFailed" to "无法加载项目。",
        "userProjectsLoadFailed" to "无法加载用户项目。",
        "updateFailed" to "无法更新项目。",
    ),
    "transport" to d(
        "title" to "交通预约",
        "backToMainAria" to "返回主界面",
        "addressToggleLabel" to "地址：",
        "addressLabel" to "地址：",
        "zipLabel" to "邮编：",
        "addressPlaceholder" to "楼栋（如有）、街道和门牌号。",
        "zipPlaceholder" to "仅 6 位数字",
        "addressBackButton" to "返回",
        "addressSubmitButton" to "保存",
        "optionInstruction" to "请选择交通类型以继续。",
        "historyTitle" to "活跃申请",
        "historyButtonLabel" to "历史",
        "historyPanelTitle" to "申请历史",
        "historyCloseButton" to "关闭",
        "kinds" to d(
            "regular" to "工作日",
            "weekend" to "周末",
            "extra" to "指定日期",
        ),
        "statusLabels" to d(
            "available" to "无申请",
            "pending" to "待处理",
            "confirmed" to "已确认",
            "realized" to "已完成",
            "rejected" to "已拒绝",
            "cancelled" to "已取消",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "周一",
                "1" to "周二",
                "2" to "周三",
                "3" to "周四",
                "4" to "周五",
                "5" to "周六",
                "6" to "周日",
            ),
            "full" to d(
                "0" to "星期一",
                "1" to "星期二",
                "2" to "星期三",
                "3" to "星期四",
                "4" to "星期五",
                "5" to "星期六",
                "6" to "星期日",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "选择日期：",
            "regularSubtitle" to "请选择此申请所需的工作日。",
            "weekendSubtitle" to "请选择此申请所需的周末日期。",
            "extraSubtitle" to "提交前请确认日期和时间。",
            "dateLabel" to "日期：",
            "timeLabel" to "时间：",
            "backButton" to "返回",
            "submitButton" to "申请",
            "requestUnavailable" to "交通申请不可用。",
            "addressRequired" to "申请交通前请先登记完整地址。",
            "dateRequiredExtra" to "请输入额外交通的日期。",
            "timeRequiredExtra" to "请输入额外交通的时间。",
            "dayRequired" to "请至少选择一天来申请交通。",
            "conflictGeneric" to "该日期已有一条有效交通申请。",
            "conflictByDate" to "{serviceDateLabel} 已存在一条有效交通申请。",
        ),
        "summary" to d(
            "noRequestRecorded" to "暂无申请记录。",
            "noActiveRequests" to "暂无活跃申请。",
            "noRequestStatus" to "无申请",
            "waitingAllocation" to "等待交通分配。",
            "vehicleAllocated" to "车辆已分配。",
            "scheduleUnavailable" to "排程不可用。",
            "requestClosed" to "申请已结束。",
            "whenRequestExists" to "有申请时会显示在这里。",
            "whenAllocated" to "分配车辆后，信息会显示在这里。",
            "departureAndLimit" to "出发 {departureTime} • 截止 {deadlineTime}",
            "limitOnly" to "截止 {deadlineTime}",
        ),
        "detail" to d(
            "title" to "申请详情",
            "genericTitle" to "交通",
            "waitingAllocation" to "等待交通分配。",
            "whenAllocated" to "分配车辆后，信息会显示在这里。",
            "inactive" to "此申请已不再有效。",
            "confirmed" to "交通已确认。",
            "realized" to "交通已完成。",
            "vehicleTypeLabel" to "车辆类型",
            "vehiclePlateLabel" to "车牌",
            "vehicleColorLabel" to "车辆颜色",
            "departureDateLabel" to "出发日期",
            "departureTimeLabel" to "出发时间",
            "unavailableValue" to "不可用",
        ),
        "actions" to d(
            "markRealized" to "已完成",
            "cancel" to "取消",
            "cancelling" to "正在取消...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "更新地址前请输入有效钥匙码。",
            "invalidKeyBeforeRequest" to "申请交通前请输入有效钥匙码。",
            "requestFailed" to "无法申请 {requestLabel}。",
            "loadFailed" to "无法检查交通信息。",
            "addressUpdated" to "地址更新成功。",
            "addressUpdateFailed" to "无法更新地址。",
            "cancelSuccess" to "交通申请已取消。",
            "cancelFailed" to "无法取消申请。",
            "requestMarkedRealized" to "申请已标记为完成。",
            "accessRequiresAuthentication" to "请输入钥匙码并验证密码以访问交通功能。",
        ),
    ),
    "status" to d(
        "validationError" to "验证错误。",
        "apiCommunicationFailure" to "API 通信失败。",
        "passwordVerifying" to "正在验证密码。",
        "authenticationCompleted" to "身份验证完成。正在更新应用...",
        "userAuthenticated" to "用户已验证。正在启动更新。",
        "applicationUpdated" to "应用更新成功。",
        "applicationUpdateFailed" to "当前无法更新应用。",
        "checkinCompleted" to "签到已完成。",
        "checkoutCompleted" to "签退已完成。",
        "savedOffline" to "已离线保存，恢复网络后将自动同步。",
        "automaticCheckinCompleted" to "自动签到已完成。",
        "automaticCheckoutCompleted" to "自动签退已完成。",
        "updatingActivitiesSequence" to "正在更新活动.....",
        "updatingLocationSequence" to "正在更新位置.....",
        "runningAutomaticActivitySequence" to "正在按需执行签到或签退.....",
        "automaticUpdatesRunning" to "更新进行中。",
        "automaticUpdatesCompletedWithActivity" to "更新已完成，并执行了 {activity}。",
        "automaticUpdatesCompletedWithoutActivity" to "更新已完成，没有执行任何活动。",
        "automaticUpdatesFailed" to "当前无法完成自动更新。",
        "automaticActivitiesDisabled" to "已启用 100% 手动模式。",
        "operationFailed" to "无法完成该操作。",
    ),
    "manual" to d(
        "eyebrow" to "Checking Web • 使用指南",
        "heading" to "使用说明",
        "introPrimary" to "本手册概述了 Checking Web 的主要认证、考勤登记、定位、交通和支持流程。",
        "introSecondary" to "请将本页作为快速参考，了解应用会自动处理哪些工作，以及哪些操作仍由用户掌控。",
        "currentLanguageLabel" to "本页语言",
        "availabilityNote" to "在当前阶段，完整手册提供葡萄牙语和英语版本。",
        "highlights" to d(
            "accessTitle" to "引导式访问",
            "accessBody" to "应用会自动判断何时需要用户注册、创建密码或进行常规认证。",
            "locationTitle" to "定位跟踪",
            "locationBody" to "权限、GPS 精度和手动回退方式都会直接影响考勤登记流程。",
            "supportTitle" to "快速帮助",
            "supportBody" to "设置集中管理语言、密码、定位、WhatsApp 支持以及本文档的访问入口。",
        ),
        "tocTitle" to "手册导航图",
        "tocAriaLabel" to "手册导航",
        "snapshotSlotLabel" to "截图位",
        "toc" to d(
            "overview" to "总览",
            "authFlow" to "认证流程",
            "userRegistration" to "用户自动注册",
            "passwordRegistration" to "密码自动注册",
            "login" to "登录",
            "attendance" to "签到与签退",
            "projectSelection" to "项目选择",
            "location" to "定位权限",
            "automaticActivities" to "自动活动",
            "transport" to "交通",
            "passwordChange" to "重置或更改密码",
            "settings" to "设置",
            "support" to "支持",
            "faq" to "常见问题与解答",
        ),
        "sections" to d(
            "overview" to d(
                "title" to "总览",
                "lead" to "Checking Web 将认证、考勤登记、定位上下文和交通访问整合在一个以手机为先的界面中。",
                "item1" to "主屏幕显示钥匙码、密码、最近历史记录、考勤登记表单以及前往交通的快捷入口。",
                "item2" to "当钥匙码尚不存在，或用户仍需创建首个密码时，辅助流程会自动出现。",
                "item3" to "设置菜单将次要操作归为一组，使主区域保持简洁。",
                "figureCaption" to "Checking Web 主界面，包含认证区域和考勤登记表单。",
            ),
            "authFlow" to d(
                "title" to "认证流程",
                "lead" to "钥匙码达到四个有效字符后，应用会立即查询其状态，并决定显示哪一种辅助流程。",
                "item1" to "如果钥匙码不存在，流程会直接进入用户注册，无需再次点击。",
                "item2" to "如果钥匙码已存在但尚无密码，界面会直接打开密码注册。",
                "item3" to "如果钥匙码和密码均已存在，界面会保持在常规登录路径上。",
                "note" to "手动关闭弹窗不会造成无限循环：只有当钥匙码或相关认证状态确实发生变化时，系统才会再次尝试打开。",
            ),
            "userRegistration" to d(
                "title" to "用户自动注册",
                "lead" to "当数据库中不存在该钥匙码时，应用会打开自助表单，供新用户完成注册。",
                "item1" to "表单会要求填写钥匙码、全名、项目、可选的电子邮件、密码以及密码确认。",
                "item2" to "项目从 API 加载，用户可以勾选一个或多个有效项。",
                "item3" to "提交成功后，网页会话即被认证，应用保持解锁状态以供使用。",
                "figureCaption" to "针对未知钥匙码自动打开的新用户注册弹窗。",
            ),
            "passwordRegistration" to d(
                "title" to "密码自动注册",
                "lead" to "如果钥匙码已存在但尚未注册密码，Checking Web 会进入首个密码创建模式。",
                "item1" to "该弹窗复用现有的密码组件，但会隐藏旧密码字段。",
                "item2" to "用户输入新密码、确认该值，即可完成首次访问。",
                "item3" to "保存后，后端会认证网页会话，主屏幕便可接受其余操作。",
                "figureCaption" to "针对系统中已存在用户的首个密码创建弹窗。",
            ),
            "login" to d(
                "title" to "登录",
                "lead" to "已注册的用户输入钥匙码和密码，由应用完成认证校验，然后再登记考勤或打开交通模块。",
                "item1" to "密码可由主界面现有流程自动校验。",
                "item2" to "如果用户在认证后更改了所输入的密码，应用会再次进入保护状态，直至新密码重新通过校验。",
                "item3" to "界面的状态提示有助于区分等待密码、正在校验和已认证这几种情况。",
            ),
            "attendance" to d(
                "title" to "签到与签退",
                "lead" to "在认证有效的情况下，用户可在主屏幕选择登记类型、核对上下文并提交操作。",
                "item1" to "表单会区分签到和签退；当自动活动主导流程时，可禁止手动提交。",
                "item2" to "顶部可见的历史记录有助于在发送新事件前确认上一条已登记事件。",
                "item3" to "每次尝试后，状态区域都会显示成功或失败的提示。",
                "figureCaption" to "考勤登记成功状态的示例。",
            ),
            "projectSelection" to d(
                "title" to "项目选择",
                "lead" to "应用在初始注册和日常使用中都会用到项目，以限定用户上下文和可用地点的范围。",
                "item1" to "在注册时，用户需先勾选一个或多个有效项目，再完成账号创建。",
                "item2" to "认证后，主面板会显示当前项目，并在该功能可用时允许更新。",
                "item3" to "活动项目会影响地点列表、上下文历史记录以及其他与用户范围相关的界面。",
                "figureCaption" to "认证后界面中项目区块的示例，显示当前生效的范围摘要。",
            ),
            "location" to d(
                "title" to "定位权限与 GPS 行为",
                "lead" to "设备位置用于确定操作上下文、触发自动流程，并在精度尚不足时引导用户。",
                "item1" to "应用需要 HTTPS、浏览器支持和已开启的权限，才能获取精确位置。",
                "item2" to "当权限被拒绝或不可用时，应用会显示清晰的提示，并可将流程限制为所允许的手动回退方式。",
                "item3" to "成功获取位置后，界面会更新精度、识别到的地点以及相关的自动操作。",
                "figureCaptionDenied" to "定位权限仍不可用时的屏幕示例。",
                "figureCaptionGranted" to "成功授予精确定位访问权限后的屏幕示例。",
            ),
            "automaticActivities" to d(
                "title" to "自动活动",
                "lead" to "当定位上下文按预期发生变化时，应用还可以触发签到、签退及相关更新。",
                "item1" to "该机制依赖有效的认证、定位读数以及避免不当切换的内部规则。",
                "item2" to "当自动活动主导流程时，部分手动字段会被隐藏或禁用，以保障记录的一致性。",
                "item3" to "如果精度低于所需阈值，应用可能会重新启用手动回退选项，而不是做出有风险的判断。",
            ),
            "transport" to d(
                "title" to "交通访问",
                "lead" to "认证后，用户可以打开交通模块来申请行程、查看状态并查阅最近一次申请的详情。",
                "item1" to "访问仍受主界面中已校验的同一钥匙码和密码保护。",
                "item2" to "该模块包含地址登记、按交通类型申请以及当前状态摘要。",
                "item3" to "当车辆完成分配时，屏幕会向用户显示主要的运营详情。",
                "figureCaption" to "从 Checking Web 主界面打开的交通模块示例。",
            ),
            "passwordChange" to d(
                "title" to "重置或更改密码",
                "lead" to "更改密码已不再放在主认证行中，现在集中在设置内。",
                "item1" to "打开设置，点击更改密码，使用现有的密码更改弹窗。",
                "item2" to "该流程会要求填写旧密码、新密码和确认密码，同时保留现有的各项校验。",
                "item3" to "仅当用户已认证且已注册密码时，该操作才会处于可用状态。",
                "figureCaption" to "从设置 > 更改密码打开的密码更改流程。",
            ),
            "settings" to d(
                "title" to "设置",
                "lead" to "齿轮图标会打开一个集中管理偏好设置和次要操作的入口，而不会让认证行显得拥挤。",
                "item1" to "语言会更新主应用的可见文字，并将该偏好保存在浏览器中。",
                "item2" to "允许定位会复用现有流程，在仍有需要时再次申请精确访问权限。",
                "item3" to "支持和关于使用同一入口来打开 WhatsApp 和本文档。",
                "figureCaption" to "包含新的集中式次要操作的设置控件。",
            ),
            "support" to d(
                "title" to "支持",
                "lead" to "当用户需要人工帮助时，设置 > 支持会准备一段 WhatsApp 对话，并在第一条消息中自动附上用户钥匙码。",
                "item1" to "该链接使用在 Checking Web 前端配置的官方电话号码。",
                "item2" to "初始消息会自动准备好，以减少沟通阻力并加快处理速度。",
                "item3" to "如果没有可用的有效钥匙码，支持按钮会出于安全考虑保持禁用。",
            ),
            "faq" to d(
                "title" to "常见问题与解答",
                "lead" to "当界面看起来卡住，或流程未按预期推进时，可参考以下快速解答。",
                "q1" to "为什么应用自己打开了一个注册流程？",
                "a1" to "这是因为钥匙码不存在，或账号尚无密码。新流程省去了多余的点击，直接带用户进入正确的操作。",
                "q2" to "为什么允许定位按钮被禁用了？",
                "a2" to "因为应用已判断精确权限处于开启状态，或者已有一次定位刷新正在进行中。",
                "q3" to "如果交通模块打不开，该怎么办？",
                "a3" to "请先确认钥匙码和密码已通过认证。如果问题仍然存在，请打开支持，以便将您的钥匙码发送给支持团队。",
            ),
            "scheduledPause" to d(
                "title" to "计划暂停",
                "lead" to "计划暂停会在某个时间段内（例如夜间）暂停自动活动，从而节省电量。",
                "item1" to "在设置中点击计划暂停。",
                "item2" to "启用该选项并设置从和到的时间（例如 22:00 至 06:00）。",
                "item3" to "如有需要，还可勾选周六暂停和/或周日暂停。在暂停期间，应用不执行任何自动活动；时间段结束后会自动恢复。",
            ),
            "accident" to d(
                "title" to "发生事故时",
                "lead" to "事故模式是一项安全功能。请仅在真正的紧急情况下使用。",
                "item1" to "任何用户都可以开启事故模式；这会实时通知同一项目中的所有用户。",
                "item2" to "报告您的状况和区域：安全、在事故现场但安全，或在事故现场且需要帮助。如有可能，请录制现场视频，它会实时发送到管理员面板。",
                "item3" to "呼叫紧急服务按钮会拨打当地紧急服务电话，用当地语言报告事故及地点。",
            ),
        ),
        "figures" to d(
            "authShellAlt" to "Checking Web 主界面，包含钥匙码和密码字段。",
            "userRegistrationAlt" to "Checking Web 的新用户注册表单。",
            "passwordRegistrationAlt" to "针对尚无密码的现有用户的首个密码注册弹窗。",
            "settingsModalAlt" to "打开的 Checking Web 设置控件，包含语言、密码、定位、支持和关于等操作。",
            "passwordChangeAlt" to "从设置打开的密码更改弹窗。",
            "locationDeniedAlt" to "定位权限被拒绝或不可用时的 Checking Web 状态。",
            "locationGrantedAlt" to "精确位置可用并已共享时的 Checking Web 状态。",
            "projectSelectionAlt" to "Checking Web 中的项目选择区域。",
            "transportScreenAlt" to "Checking Web 生态系统内的交通模块屏幕。",
            "checkSuccessAlt" to "Checking Web 中签到或签退成功的状态。",
        ),
    ),
    "accident" to d(
        "button" to d(
            "report" to "报告事故",
            "reported" to "事故已报告",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "我需要有关 Web 应用的帮助。我的钥匙码是 {chave}。",
    ),
    "instructions" to d(
        "heading" to "使用说明",
        "intro" to "本指南将一步步说明如何使用 Checking：手动登记考勤、开启自动模式（根据位置自动签到和签退）以及设置计划暂停。",
        "step1" to d(
            "title" to "1. 登录应用",
            "item1" to "在首页的'钥匙码'栏中输入您的 4 位钥匙码。找到该钥匙码时，输入框会变为橙色。",
            "item2" to "在'密码'栏中输入密码。验证通过后，输入框变为绿色，并显示'验证完成'。",
            "item3" to "如果您还没有密码，应用会自动打开密码创建流程；如果钥匙码不存在，则会提供自助注册。",
        ),
        "step2" to d(
            "title" to "2. 手动登记考勤",
            "item1" to "选择'签到'或'签退'，以及类型'正常'或'补录'。",
            "item2" to "关闭自动模式后，在列表中选择'地点'，然后点击'登记签到'（或'签退'）。",
            "item3" to "顶部卡片显示您最近的签到和签退；点击它即可查看包含日期、时间和地点的完整列表。",
        ),
        "step3" to d(
            "title" to "3. 开启自动模式",
            "lead" to "开启自动模式后，应用会根据您的位置自动签到和签退——在进入或离开已登记区域时、将应用切到前台时以及定期检查时。",
            "item1" to "点击齿轮图标（位于钥匙码/密码栏旁）打开'设置'。",
            "item2" to "点击'自动活动'，勾选'启用自动活动'。",
            "item3" to "逐项点击并授予列表中的每项权限：通知、'始终允许'的位置权限、不受限制的电池使用，以及在部分设备上的'随设备启动'。",
            "item4" to "当齿轮显示绿色光晕时，自动模式处于正常运行状态；橙色光晕表示缺少某项建议权限。",
            "callout" to "重要提示：为了在后台稳定运行，请将位置权限设为'始终允许'，并关闭对 Checking 的电池优化。",
        ),
        "step4" to d(
            "title" to "4. 启用计划暂停",
            "lead" to "计划暂停会在某个时间段内（例如夜间）暂停自动活动，从而节省电量。",
            "item1" to "在'设置'中点击'计划暂停'。",
            "item2" to "启用该选项并设置'从'和'到'的时间（例如 22:00 至 06:00）。",
            "item3" to "如有需要，还可勾选'周六暂停'和/或'周日暂停'。",
            "item4" to "在暂停期间，应用不执行任何自动活动；时间段结束后会自动恢复。",
        ),
        "step5" to d(
            "title" to "5. 查看历史记录",
            "item1" to "点击'最近签到'或'最近签退'，打开包含每条记录的日期、时间和地点的表格。",
            "item2" to "在已登记区域附近但区域之外完成的记录会显示为'未登记位置'。",
            "item3" to "即使没有网络，您的记录也会保存在设备上，并在网络恢复后立即发送，始终使用原始时间。",
        ),
        "step6" to d(
            "title" to "6. 申请交通",
            "item1" to "点击'交通'打开人员交通模块。",
            "item2" to "填写所需的地址和时间并提交申请。",
            "item3" to "物流负责人安排行程；人工智能引擎会建议如何分组乘客并排列停靠顺序。",
        ),
        "step7" to d(
            "title" to "7. 发生事故时",
            "lead" to "事故模式是一项安全功能。请仅在真正的紧急情况下使用。",
            "item1" to "任何用户都可以开启事故模式；这会实时通知同一项目中的所有用户。",
            "item2" to "报告您的状况和区域：'安全'、'在事故现场但安全'或'在事故现场且需要帮助'。",
            "item3" to "如有可能，请录制现场视频：它会实时发送到管理员面板。",
            "item4" to "'呼叫紧急服务'按钮会拨打当地紧急服务电话，用当地语言报告事故及地点。",
        ),
        "step8" to d(
            "title" to "8. 其他设置",
            "item1" to "'通知'：选择要接收哪些通知（活动、计划暂停、事故）。",
            "item2" to "'语言'：切换应用语言。",
            "item3" to "'更改密码'：在需要时设置新密码。",
            "item4" to "'支持'：通过 WhatsApp 直接联系团队。",
            "item5" to "'关于'：了解 Checking 的历史以及构成系统的各个部分。",
        ),
        "closing" to "完成！开启自动模式后，您无需手动登记任何内容——Checking 会替您完成。",
    ),
    "about" to d(
        "heading" to "关于 Checking",
        "introTitle" to "Checking 的由来",
        "introBody" to "Checking 于 2026 年 3 月开始开发，源自工程师 Dilnei Schmidt 的构想。\n" +
            "\n" +
            "当时需要在发生事故时，快速识别在建设和安装作业现场的所有巴西国家石油公司（Petrobras）员工。\n" +
            "\n" +
            "SMS（健康、安全与环境）管理团队最初的方案是一个在线表单，由每位员工在到达和离开工作现场时填写。它能够识别在场人员，但操作繁琐，许多员工偶尔会忘记填写。\n" +
            "\n" +
            "为提高效率，Dilnei 开发了一款应用，能够：\n" +
            "• 通过 GPS 识别用户与工作现场的距离，并提醒其需要签到；\n" +
            "• 在典型的签到和签退时间预设闹钟，提醒用户填写表单；\n" +
            "• 用用户的数据自动填写表单并在线提交。\n" +
            "\n" +
            "这简化了工作，提高了表单的填写频率。\n" +
            "\n" +
            "同样在 2026 年 3 月，工程师 Tamer Salmem 了解到这些已实施的方案，并运用当前的编程技术加以推进，开发出最初由 Dilnei 构想的系统。\n" +
            "\n" +
            "目标是让用户无需操心打开应用去签到或签退。此外，还要建立实时监控，使管理员不仅能知道谁在工作，还能实时知道每位用户处于各项目已登记地点中的哪一个——从而提升应急响应能力。\n" +
            "\n" +
            "于是，系统增加了以下功能：\n" +
            "• 通过地理围栏激活服务（根据用户与工作现场的距离）；\n" +
            "• 后台执行任务——在设施内每次位置变化时签到，离开时签退，用户甚至无需解锁设备；\n" +
            "• 实时将用户位置发送至管理员面板；\n" +
            "• 可在世界任何地方登记任意数量的项目。\n" +
            "\n" +
            "系统还可进入'事故模式'。发生事故时，任何用户都可触发警报，实时通知同一项目中的所有用户。事故模式开启后：\n" +
            "• 管理员面板上会创建一张表格，列出每位用户的状况：'安全'、'在事故现场但安全'以及'在事故现场且需要帮助'；\n" +
            "• 用户可录制视频并实时发送，作为表格中的链接，供管理员查看现场画面；\n" +
            "• '呼叫紧急服务'按钮会拨打已登记的当地紧急服务电话，用当地语言报告事故、地点及负责人联系方式。\n" +
            "\n" +
            "系统的稳健性和可靠性为 Petrobras 的 SMS 团队带来了运营安全和即时响应能力。\n" +
            "\n" +
            "最后，工程师 Thiago Soares do Nascimento 将系统产生的信息与现有的管理仪表板整合，使新系统与原有的表单填写协同工作，保持管理控制的更新。\n" +
            "\n" +
            "CHECKING 就此诞生。",
        "partsTitle" to "系统的组成部分",
        "partsIntro" to "Checking 是一套考勤管理系统，记录员工进出工作现场的情况。它通过多种渠道运作——现场安装的 RFID 读卡器、一款 Android 应用、可在手机上访问的网页以及一个管理面板——并将所有内容汇集于一处。\n" +
            "\n" +
            "系统由以下部分组成：\n" +
            "• 一个用 Python/FastAPI 开发的 API；\n" +
            "• 一个面向系统管理员的网站；\n" +
            "• 一个适配手机和桌面的 Web 应用；\n" +
            "• 一个用于人员交通管理的仪表板；\n" +
            "• 一款用 Kotlin 开发的 Android 专用应用。",
        "partApiTitle" to "API",
        "partApiBody" to "API 是系统的大脑。每当有人签到或签退——无论通过实体读卡器、应用还是网页——都由它接收信息、核验是否正确、保存到数据库，并实时通知其他组件。\n" +
            "\n" +
            "它还会在每次记录后自动填写企业的 Microsoft Forms 表单，协调交通系统，在发生事故时触发紧急警报，并确保在连接不稳定时不丢失任何数据。",
        "partWebsiteTitle" to "网站",
        "partWebsiteBody" to "网站是管理员的控制面板。通过它可以实时查看谁已签到、谁已签退，并在无需技术知识的情况下管理系统的各个方面。\n" +
            "\n" +
            "主要功能：登记和编辑员工、创建项目及其规则、定义系统识别的地理区域、查看考勤报表以及导出数据。它也是触发并跟进事故模式的中枢——实时查看每位员工的状况并协调应急响应。",
        "partWebappTitle" to "Web 应用",
        "partWebappBody" to "Web 应用是员工使用的工具。它在手机或电脑的浏览器中运行，无需安装任何程序，可登记进出、查看历史记录并申请交通。\n" +
            "\n" +
            "员工开启自动活动后，手机会自动检测位置，并在进出已登记区域时自动签到或签退。发生事故时，界面会切换，要求员工报告其状况和安全区域。\n" +
            "\n" +
            "它提供六种语言（葡萄牙语、英语、中文、马来语、印尼语和他加禄语），以服务国际团队。",
        "partTransportTitle" to "交通仪表板",
        "partTransportBody" to "交通仪表板是负责出行物流的人员使用的工具。通过它可以登记车辆、查看并组织员工提交的交通申请，以及为每个人安排当天的车辆。\n" +
            "\n" +
            "它内置一个人工智能引擎，分析地址和时间，自动建议如何分组乘客并以优化方式排列停靠点——减少出行时间和行程数量。负责人可以接受建议、进行调整或手动安排。",
        "partAndroidTitle" to "Android 应用",
        "partAndroidBody" to "Android 应用提供与 Web 应用相同的功能，但日常体验更完整。其主要优势是基于地理定位的自动化：应用在后台运行，随员工进出已登记区域自动记录签到或签退，无需依赖浏览器。\n" +
            "\n" +
            "它还可离线工作：没有网络时，记录会保存在手机上，并在网络恢复后立即发送，始终使用原始时间。它还包含带有每个事件日期、时间和地点的历史记录、交通模块以及用于事故的紧急模式。",
        "rulesTitle" to "签到与签退的情形",
        "rulesIntro" to "以下情形逐步说明在每个典型场景中，系统应为每位用户执行的操作（签到或签退）。Web 应用和原生应用各自遵循对应模块的规则。",
        "rulesWebTitle" to "情形 — Web 应用",
        "rulesWebBody" to "## 情形 1 — 离开时签退\n" +
            "• 自动活动已开启，并拥有完整的位置权限。\n" +
            "• 上一次活动是签到。\n" +
            "• 应用更新位置，发现用户位于'签退区'或距任何已登记地点超过 2 公里（签退区除外）。\n" +
            "• 由于上一次活动是签到，应用为用户签退。\n" +
            "\n" +
            "## 情形 2 — 已签退，距离较远或在签退区\n" +
            "• 上一次活动是签退。\n" +
            "• 用户位于'签退区'或距任何已登记地点超过 2 公里。\n" +
            "• 不执行任何操作：签退不会因位置变化而重复。\n" +
            "\n" +
            "## 情形 3 — 到达工作地点（签到）\n" +
            "• 上一次活动是签退。\n" +
            "• 用户位于'签退区'以外的某个已登记区域内部（与该区域实际匹配，而非仅仅靠近）。\n" +
            "• 用户确实在工作现场（包括当天的第一次签到）。\n" +
            "• 应用为用户签到，并将位置更新为相应的已登记区域。\n" +
            "! 重要提示：如果用户不在任何已登记区域内——即使靠近（距某坐标不足 2 公里，签退区除外）——应用不会自动签到；只会显示'未登记位置'（与情形 5 相同）。\n" +
            "\n" +
            "## 情形 4 — 新的签到（始终）\n" +
            "• 上一次活动是签到。\n" +
            "• 用户位于'签退区'以外的某个已登记区域。\n" +
            "• 无论位置是否变化，应用都会执行一次新的签到。\n" +
            "• 即使在与上次签到相同的地点，也会执行新签到，以记录/更新位置和时间。\n" +
            "\n" +
            "## 情形 5 — 靠近但在区域之外\n" +
            "• 上一次活动是签到。\n" +
            "• 用户不在任何已登记区域，但距某已登记坐标也未超过 2 公里（签退区除外）。也就是说，用户靠近工作地点。\n" +
            "• 不执行任何操作：应用仅显示'未登记位置'。\n" +
            "\n" +
            "## 情形 6 — 签到后点击'刷新'\n" +
            "• 应用已在前台；上一次活动是签到。\n" +
            "• 用户点击'刷新'以更新位置。\n" +
            "• 无论位置是否变化，应用都会执行一次新的签到，以记录/更新位置和时间。\n" +
            "\n" +
            "## 情形 7 — 离开签退区\n" +
            "• 在前台；上一次活动是签退；用户位于'签退区'（不执行操作）。\n" +
            "• 用户点击'刷新'，应用发现其已离开签退区，前往：\n" +
            "• 变体 7A——'签退区'以外的某个已登记区域；\n" +
            "• 变体 7B——不在任何已登记区域，但仍然靠近（不足 2 公里，签退区除外）。\n" +
            "• 两种情况下，应用都会立即签到，将位置更新为已登记区域；若无精确匹配，则更新为'未登记位置'。\n" +
            "\n" +
            "## 情形 8 — 混合区\n" +
            "• 应用检测到位置与'混合区'匹配（首次进入或连续读取）。\n" +
            "• 如果上一次相关活动不在'混合区'内，则立即切换：8A——签到后→在'混合区'签退；8B——签退后→在'混合区'签到。\n" +
            "• '混合区时间间隔'字段（管理网站'登记'选项卡）是仅适用于混合区内连续读取的冷却时间：当 已用时间 < 间隔 时，阻止新的切换；当 >= 间隔 时，重新允许。\n" +
            "• 混合区签到后的例外：前往'签退区'或超出'自动签退最小距离'→立即签退，无需等待冷却。\n" +
            "• 混合区签退后的例外：前往其他已登记区域（签退区和混合区除外），或仍处于最小距离内→立即签到，放弃冷却。\n" +
            "\n" +
            "## 情形 9 — 手动模式（自动活动关闭）\n" +
            "• 用户已通过验证；自动活动已关闭。\n" +
            "• 如有权限，应用会更新位置；否则显示'权限被拒绝'。\n" +
            "• 用户选择'签到'或'签退'、'正常'或'补录'，选择'地点'（自动活动关闭时始终可用），然后点击'登记'。\n" +
            "• 应用按照所选项执行常规流程。",
        "rulesNativeTitle" to "情形 — 原生应用（Android）",
        "rulesNativeBody" to "## 情形 1 — 离开时签退\n" +
            "• 上一次活动是签到。\n" +
            "• 用户位于'签退区'或距任何已登记地点超过 2 公里（签退区除外）。\n" +
            "• 应用为用户签退。\n" +
            "\n" +
            "## 情形 2 — 已签退，距离较远或在签退区\n" +
            "• 上一次活动是签退。\n" +
            "• 用户位于'签退区'或距任何已登记地点超过 2 公里。\n" +
            "• 不执行任何操作：签退不会因位置变化而重复。\n" +
            "\n" +
            "## 情形 3 — 到达工作地点（签到）\n" +
            "• 上一次活动是签退。\n" +
            "• 用户位于'签退区'以外的某个已登记区域内部（实际匹配，而非仅仅靠近）。\n" +
            "• 应用为用户签到，并将位置更新为相应区域。\n" +
            "! 重要提示：从签退状态出发，如果用户不在任何已登记区域内——即使靠近——应用不会签到；只会显示'未登记位置'（参见变体 7B）。当上一次活动是签到、而用户靠近但在区域之外时，行为不同：应用会以'未登记位置'作为变化进行一次签到（参见情形 5）。\n" +
            "\n" +
            "## 情形 4 — 仅在位置变化时新签到\n" +
            "• 上一次活动是签到。\n" +
            "• 用户位于'签退区'以外的某个已登记区域。\n" +
            "• 仅当该区域与上次签到的区域不同时，应用才执行新的签到。\n" +
            "• 在与上次签到相同的地点，不执行任何操作（这消除了重复签到）。更换区域时，新签到会记录/更新位置和时间。\n" +
            "\n" +
            "## 情形 5 — 靠近但在区域之外（延续）\n" +
            "• 上一次活动是签到。\n" +
            "• 用户不在任何已登记区域，但靠近（距某坐标不足 2 公里，签退区除外）。\n" +
            "• 由于离开了区域，应用会以'未登记位置'进行一次签到，记录用户行程的连续性。\n" +
            "• 仅作为变化发生：如果上次签到已是'未登记位置'，则不执行任何操作（不重复）。\n" +
            "\n" +
            "## 情形 6 — 签到后点击'刷新'\n" +
            "• 在前台；上一次活动是签到。\n" +
            "• 用户点击'刷新'。\n" +
            "• 仅当位置与上次签到不同时才新签到（与情形 4 规则相同）。在相同地点不执行任何操作。\n" +
            "\n" +
            "## 情形 7 — 离开签退区\n" +
            "• 在前台；上一次活动是签退；用户位于'签退区'（不执行操作）。\n" +
            "• 用户点击'刷新'，应用将位置更新为：变体 7A——'签退区'以外的某个已登记区域；变体 7B——不在任何已登记区域，但仍靠近（不足 2 公里，签退区除外）。\n" +
            "• 7A：由于上一次活动是签退，应用立即在相应区域签到。\n" +
            "• 7B：由于用户处于签退状态且不在任何区域内，应用不签到；只显示'未登记位置'（与情形 3 的提示规则相同）。\n" +
            "\n" +
            "## 情形 8 — 混合区\n" +
            "• 应用检测到'混合区'，如果上一次相关活动不在其中，则立即切换：8A——签到后→在'混合区'签退；8B——签退后→在'混合区'签到。\n" +
            "• '混合区时间间隔'是仅适用于混合区内连续读取的冷却时间：当 已用时间 < 间隔 时阻止新切换；当 >= 间隔 时重新允许。\n" +
            "• 立即例外（放弃冷却）：前往'签退区'或超出最小距离→签退；前往其他已登记区域或仍在最小距离内→签到。\n" +
            "\n" +
            "## 情形 9 — 手动模式（自动活动关闭）\n" +
            "• 用户已验证；自动活动已关闭。\n" +
            "• 如有权限，应用会更新位置；否则显示'权限被拒绝'。\n" +
            "• 用户选择签到/签退、正常/补录，选择'地点'，然后点击'登记'。\n" +
            "• 应用按所选项执行常规流程。",
        "notesTitle" to "通用说明",
        "notesBody" to "## 前台触发\n" +
            "• 在自动活动已开启且用户已验证的情况下，打开应用或将其切到前台会触发自动评估（引擎按上述情形决定签到或签退）。地理围栏和每 15 分钟的定期检查同理。\n" +
            "• 不存在'盲目'的定期签到：每 15 分钟的检查始终核验位置并保持'无变化则跳过'规则。\n" +
            "\n" +
            "## 仅在位置变化时签到\n" +
            "• 仅当解析出的位置与上次签到不同时，才会自动签到。位置相同→不执行操作。该规则（情形 4 和 6）正是消除重复签到的原因。\n" +
            "\n" +
            "## 按项目填写表单\n" +
            "• 在当天第一次签到和每次签退时，会为用户已登记的每个项目各填写并提交一次表单（遵循各项目的'已启用表单'设置）。例如：用户在 P80 和 P83 项目中→提交两次。单项目用户→提交一次。\n" +
            "\n" +
            "## 签退不变量（保持不变）\n" +
            "• 在所有所述情形下都会自动签退（签退区、超出限制的距离、混合区切换）；绝不会连续两次签退；签退之后，下一次自动活动始终是签到。",
    ),
)

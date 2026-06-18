package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun enDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Checking Manual",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "Check-in and check-out registration form",
        "credentialsAria" to "User identification and password",
        "keyLabel" to "Key",
        "passwordLabel" to "Password",
        "keyPlaceholder" to "Ex.: HR70",
        "passwordPlaceholder" to "3 to 10 characters",
        "requestRegistrationButton" to "Request registration",
        "settingsSpacer" to "Settings",
        "openSettingsAria" to "Open settings",
        "openSettingsTitle" to "Open settings",
        "waitingAuthentication" to "Waiting for authentication.",
        "enterPasswordPrompt" to "Enter your password to start.",
        "createPasswordPrompt" to "Enter your key and create a password.",
        "invalidFourCharacterKey" to "Enter a 4-character alphanumeric key.",
        "unknownUserDetail" to "The user key is not registered",
        "transportAccessPrompt" to "Enter your key and validate the password to access Transport.",
    ),
    "history" to d(
        "lastCheckinLabel" to "Last Check-In",
        "lastCheckoutLabel" to "Last Check-Out",
        "today" to "Today",
        "yesterday" to "Yesterday",
        "dialogTitleCheckin" to "Check-In History",
        "dialogTitleCheckout" to "Check-Out History",
        "colDate" to "Date",
        "colTime" to "Time",
        "colLocal" to "Location",
        "empty" to "No records found.",
        "back" to "Back",
        "loadingMessage" to "Loading history...",
        "notFoundMessage" to "No record was found for this key.",
        "noRecordsMessage" to "No check-in or check-out was recorded for this key.",
        "updatedMessage" to "History updated for the informed key.",
        "loadFailed" to "Could not check the history for this key.",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "Automatic Activities",
        "sectionTitle" to "Registration",
        "checkinLabel" to "Check-In",
        "checkoutLabel" to "Check-Out",
        "transportLabel" to "Transport",
        "informeTitle" to "Informe",
        "informeNormalLabel" to "Normal",
        "informeRetroativoLabel" to "Backdated",
        "submitButton" to "Register",
        "checkInLowerLabel" to "check-in",
        "checkOutLowerLabel" to "check-out",
        "disableAutomaticActivitiesForManualSubmit" to "Disable Automatic Activities to register manually.",
        "selectLocationBeforeSubmit" to "Select a location before registering.",
    ),
    "settings" to d(
        "title" to "Settings",
        "languageLabel" to "Language",
        "resetPasswordLabel" to "Change Password",
        "allowLocationLabel" to "Allow Location",
        "allowAudioVideoLabel" to "Allow Audio & Video",
        "notificationsLabel" to "Alerts",
        "instructionsLabel" to "Instructions",
        "supportLabel" to "Support",
        "manualLabel" to "Full manual",
        "aboutLabel" to "About",
        "backButton" to "Back",
        "groupAutoActivities" to "Automatic Activities",
        "groupPreferences" to "Preferences",
        "groupHelp" to "Help",
        "statusOn" to "Active",
        "statusAttention" to "Attention",
        "statusOff" to "Disabled",
    ),
    "notifications" to d(
        "title" to "Alerts",
        "intro" to "Enable notifications to be aware whenever Checking performs an activity automatically, or to stay on top of important events. Enable 'push' notifications to:",
        "checkboxActivities" to "be notified when an activity is performed automatically.",
        "checkboxScheduledPause" to "know when the app starts or ends 'Scheduled Pause' mode.",
        "checkboxAccident" to "know when an accident is reported.",
        "backButton" to "Back",
    ),
    "permissions" to d(
        "title" to "Permissions",
        "locationText" to "Sharing your exact/precise location lets the app identify which registered region you are in. Choose 'ALLOW ALL THE TIME' and turn on 'Use precise location'.",
        "locationButton" to "Precise Location",
        "cameraMicText" to "The device camera and microphone are only used if you choose to record a video of an accident, to inform the administrators.",
        "cameraButton" to "Camera",
        "microphoneButton" to "Microphone",
        "autoStartText" to "Enabling \"Auto Start\" lets Android launch Checking automatically every time the device is turned on or restarted.",
        "autoStartButton" to "Auto Start",
        "batteryText" to "If any battery saver mode is active, background activities may be impaired.",
        "batteryButton" to "Battery Restrictions",
        "backgroundText" to "Allowing the app to stay active in the background makes activities run even when the device is locked.",
        "backgroundButton" to "Background",
        "backgroundOpsText" to "For Checking to work flawlessly in the background — performing activities even with the screen locked or after the app is closed, and restarting with the device — grant the following: run in the background, remove battery restrictions, and start automatically.",
        "backgroundOpsButton" to "Background Operation",
        "notificationsText" to "Notifications tell you when activities are performed.",
        "notificationsButton" to "Notifications",
        "exactAlarmText" to "Exact alarms let Checking resume automatic activities at the exact time the Scheduled Pause ends.",
        "exactAlarmButton" to "Exact Alarms",
        "statusButton" to "Permission Status",
        "statusTitle" to "Permission Status",
        "backButton" to "Back",
        "statusLocation" to "Location",
        "statusCameraMic" to "Camera & Microphone",
        "statusAutoStart" to "Auto-Start",
        "statusBattery" to "Battery Restrictions",
        "statusBackground" to "Background",
        "statusNotifications" to "Notifications",
        "statusExactAlarm" to "Exact Alarms",
        "locationPrecise" to "precise allowed",
        "locationPreciseNoBackground" to "precise, but not 'Allow all the time'",
        "locationImprecise" to "approximate allowed",
        "locationDenied" to "not allowed",
        "cameraMicGranted" to "allowed",
        "cameraMicDenied" to "not allowed",
        "autoStartOn" to "enabled",
        "autoStartOff" to "not enabled",
        "batteryRestricted" to "restricted",
        "batteryUnrestricted" to "not restricted",
        "backgroundAllowed" to "allowed",
        "backgroundDisallowed" to "not allowed",
        "notificationsAllowed" to "allowed",
        "notificationsDisallowed" to "not allowed",
        "exactAlarmAllowed" to "allowed",
        "exactAlarmDisallowed" to "not allowed",
    ),
    "passwordDialog" to d(
        "titleChange" to "Change Password",
        "titleRegister" to "Create Password",
        "oldPasswordLabel" to "Old Password",
        "newPasswordLabel" to "New Password",
        "confirmPasswordLabel" to "Confirm Password",
        "backButton" to "Back",
        "submitChangeButton" to "Change",
        "submitRegisterButton" to "Save",
        "changingStatus" to "Changing password...",
        "savingStatus" to "Saving password...",
        "validatingStatus" to "Verifying password.",
        "oldPasswordInvalid" to "The old password must be between 3 and 10 characters.",
        "newPasswordInvalid" to "The new password must be between 3 and 10 characters.",
        "confirmMismatch" to "The new password confirmation does not match.",
        "changeFailed" to "Could not change the password.",
        "validationFailed" to "Could not validate the password.",
        "statusLoadFailed" to "Could not check the password status.",
    ),
    "registrationDialog" to d(
        "title" to "Request Registration",
        "note" to "Fill in the information below to use the Checking system.",
        "keyLabel" to "Key",
        "fullNameLabel" to "Full Name",
        "projectsLabel" to "Projects",
        "projectsHint" to "Select one or more projects.",
        "emailLabel" to "Email",
        "emailPlaceholder" to "Optional",
        "passwordLabel" to "Password",
        "confirmPasswordLabel" to "Confirm Password",
        "backButton" to "Back",
        "submitButton" to "Send",
        "loadingProjects" to "Loading projects...",
        "noProjectsAvailable" to "No project is currently available.",
        "fullNameRequired" to "Enter the full name.",
        "emailInvalid" to "Enter a valid email or leave the field blank.",
        "passwordInvalid" to "The password must be between 3 and 10 characters.",
        "confirmMismatch" to "The new password confirmation does not match.",
        "submittingStatus" to "Sending registration request...",
        "successStatus" to "Registration completed successfully.",
        "submitFailed" to "Could not send the registration request.",
    ),
    "location" to d(
        "title" to "Location",
        "waitingLabel" to "Waiting for location.",
        "refreshLabel" to "Refresh location",
        "refreshBusyLabel" to "Refreshing location",
        "unavailableShort" to "Unavailable",
        "unavailableLabel" to "Location unavailable",
        "unavailableMessage" to "Could not check the location right now.",
        "noPermissionLabel" to "No Permission",
        "timeoutLabel" to "Timed Out",
        "timeoutMessage" to "Location lookup took longer than expected.",
        "detectingLabel" to "Detecting...",
        "exactConfirmationBrowser" to "Waiting for exact-location confirmation from the browser.",
        "exactConfirmationApp" to "Waiting for exact-location confirmation from the shortcut/app.",
        "updatingDeviceLocation" to "Updating the device current location.",
        "secureContextRequired" to "Precise location requires a secure connection (HTTPS).",
        "browserUnsupported" to "This browser does not support precise location.",
        "permissionBlocked" to "Location permission is blocked in the browser. Enable site access in the browser settings.",
        "captureRequiresSupport" to "Location capture requires HTTPS and browser support.",
        "noValidPosition" to "Could not obtain a valid device position.",
        "searchingPrecision" to "Searching for enough precision...",
        "completionStatus" to "Location update completed.",
        "completionStatusWithDetail" to "Location update completed. {detail}",
        "browserContextLabel" to "in this browser",
        "appContextLabel" to "in this shortcut/app",
        "browserSourceLabel" to "through the browser",
        "appSourceLabel" to "through the shortcut/app",
        "currentAccuracyLabel" to "Current accuracy",
        "accuracyPrefix" to "Accuracy",
        "accuracyTemplate" to "Accuracy {accuracy}",
        "accuracyLimitTemplate" to "Limit {limit} m",
        "accuracyCombinedTemplate" to "Accuracy {accuracy} / Limit {limit} m",
        "noKnownLocations" to "No registered locations",
        "defaultManualLocationLabel" to "Main Office",
        "accuracyFallbackManualLocationLabel" to "Insufficient Accuracy",
        "outsideWorkplaceLabel" to "Outside Workplace",
        "unregisteredLocationLabel" to "Unregistered Location",
        "mixedZoneLabel" to "Mixed Zone",
        "checkoutZoneLabel" to "Checkout Zone",
    ),
    "projects" to d(
        "label" to "Projects",
        "changeButton" to "Change",
        "loadingProjects" to "Loading projects...",
        "updatingProjects" to "Updating projects...",
        "noneAvailableShort" to "No project available",
        "noneAvailableSentence" to "No project available.",
        "noneAvailableNow" to "No project is currently available.",
        "selectAtLeastOne" to "Select at least one project.",
        "userProjectsAria" to "User projects",
        "registrationProjectsAria" to "Registration projects",
        "updatedSuccess" to "Projects updated successfully.",
        "loadFailed" to "Could not load the projects.",
        "userProjectsLoadFailed" to "Could not load the user projects.",
        "updateFailed" to "Could not update the projects.",
    ),
    "transport" to d(
        "title" to "Transport Scheduling",
        "backToMainAria" to "Back to the main screen",
        "addressToggleLabel" to "Address:",
        "addressLabel" to "Address:",
        "zipLabel" to "ZIP Code:",
        "addressPlaceholder" to "Block (if any), street, and number.",
        "zipPlaceholder" to "Only 6 digits",
        "addressBackButton" to "Back",
        "addressSubmitButton" to "Save",
        "optionInstruction" to "Select the transport type to continue.",
        "historyTitle" to "Active requests",
        "historyButtonLabel" to "History",
        "historyPanelTitle" to "Request History",
        "historyCloseButton" to "Close",
        "kinds" to d(
            "regular" to "Weekdays",
            "weekend" to "Weekend",
            "extra" to "Specific Date",
        ),
        "statusLabels" to d(
            "available" to "No request",
            "pending" to "Pending",
            "confirmed" to "Confirmed",
            "realized" to "Completed",
            "rejected" to "Rejected",
            "cancelled" to "Cancelled",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "Mon",
                "1" to "Tue",
                "2" to "Wed",
                "3" to "Thu",
                "4" to "Fri",
                "5" to "Sat",
                "6" to "Sun",
            ),
            "full" to d(
                "0" to "Monday",
                "1" to "Tuesday",
                "2" to "Wednesday",
                "3" to "Thursday",
                "4" to "Friday",
                "5" to "Saturday",
                "6" to "Sunday",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "Select the days:",
            "regularSubtitle" to "Select the weekday days you want for this request.",
            "weekendSubtitle" to "Select the weekend days you want for this request.",
            "extraSubtitle" to "Review the date and time before requesting.",
            "dateLabel" to "Date:",
            "timeLabel" to "Time:",
            "backButton" to "Back",
            "submitButton" to "Request",
            "requestUnavailable" to "Transport request is unavailable.",
            "addressRequired" to "Register a complete address before requesting transport.",
            "dateRequiredExtra" to "Enter the date for extra transport.",
            "timeRequiredExtra" to "Enter the time for extra transport.",
            "dayRequired" to "Select at least one day to request transport.",
            "conflictGeneric" to "There is already an active transport request for that date.",
            "conflictByDate" to "There is already an active transport request for {serviceDateLabel}.",
        ),
        "summary" to d(
            "noRequestRecorded" to "No request recorded.",
            "noActiveRequests" to "No active requests.",
            "noRequestStatus" to "No request",
            "waitingAllocation" to "Waiting for transport allocation.",
            "vehicleAllocated" to "Vehicle allocated.",
            "scheduleUnavailable" to "Schedule unavailable.",
            "requestClosed" to "Request closed.",
            "whenRequestExists" to "When a request exists, it will appear here.",
            "whenAllocated" to "When you are assigned to a vehicle, the information will appear here.",
            "departureAndLimit" to "Departure {departureTime} • Limit {deadlineTime}",
            "limitOnly" to "Limit {deadlineTime}",
        ),
        "detail" to d(
            "title" to "Request Details",
            "genericTitle" to "Transport",
            "waitingAllocation" to "Waiting for transport allocation.",
            "whenAllocated" to "When you are assigned to a vehicle, the information will appear here.",
            "inactive" to "This request is no longer active.",
            "confirmed" to "Transport confirmed.",
            "realized" to "Transport completed.",
            "vehicleTypeLabel" to "Vehicle Type",
            "vehiclePlateLabel" to "Vehicle Plate",
            "vehicleColorLabel" to "Vehicle Color",
            "departureDateLabel" to "Departure Date",
            "departureTimeLabel" to "Departure Time",
            "unavailableValue" to "Unavailable",
        ),
        "actions" to d(
            "markRealized" to "Completed",
            "cancel" to "Cancel",
            "cancelling" to "Cancelling...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "Enter a valid key before updating the address.",
            "invalidKeyBeforeRequest" to "Enter a valid key before requesting transport.",
            "requestFailed" to "Could not request {requestLabel}.",
            "loadFailed" to "Could not check transport.",
            "addressUpdated" to "Address updated successfully.",
            "addressUpdateFailed" to "Could not update the address.",
            "cancelSuccess" to "Transport request cancelled.",
            "cancelFailed" to "Could not cancel the request.",
            "requestMarkedRealized" to "Request marked as completed.",
            "accessRequiresAuthentication" to "Enter your key and validate the password to access Transport.",
        ),
    ),
    "status" to d(
        "validationError" to "Validation error.",
        "apiCommunicationFailure" to "API communication failed.",
        "passwordVerifying" to "Verifying password.",
        "authenticationCompleted" to "Authentication completed.",
        "updatingApp" to "Updating the application...",
        "userAuthenticated" to "User authenticated. Starting updates.",
        "applicationUpdated" to "Application updated successfully.",
        "applicationUpdateFailed" to "Could not update the application right now.",
        "checkinCompleted" to "Check-In completed.",
        "checkoutCompleted" to "Check-Out completed.",
        "savedOffline" to "Saved offline. It will sync when you're back online.",
        "automaticCheckinCompleted" to "Automatic Check-In completed.",
        "automaticCheckoutCompleted" to "Automatic Check-Out completed.",
        "updatingActivitiesSequence" to "Updating activities.....",
        "updatingLocationSequence" to "Updating location.....",
        "runningAutomaticActivitySequence" to "Running check-in or check-out when applicable.....",
        "automaticUpdatesRunning" to "Update in progress.",
        "automaticUpdatesCompletedWithActivity" to "Updates completed with {activity} performed.",
        "automaticUpdatesCompletedWithoutActivity" to "Updates completed with no activities performed.",
        "automaticUpdatesFailed" to "Could not complete the automatic updates right now.",
        "automaticActivitiesDisabled" to "100% manual mode is now active.",
        "operationFailed" to "Could not complete the operation.",
    ),
    "manual" to d(
        "eyebrow" to "Checking Web • User guide",
        "heading" to "Checking Web Manual",
        "introPrimary" to "This manual summarizes the main authentication, attendance, location, transport, and support flows of Checking Web.",
        "introSecondary" to "Use this page as a quick reference to understand what the application handles automatically and which actions remain under user control.",
        "currentLanguageLabel" to "Page language",
        "availabilityNote" to "At this stage, the full manual is available in Portuguese and English.",
        "highlights" to d(
            "accessTitle" to "Guided access",
            "accessBody" to "The app decides automatically when to ask for user registration, password creation, or regular authentication.",
            "locationTitle" to "Tracked location",
            "locationBody" to "Permissions, GPS accuracy, and manual fallback directly influence the attendance workflow.",
            "supportTitle" to "Fast help",
            "supportBody" to "Settings centralizes language, password, location, WhatsApp support, and access to this documentation.",
        ),
        "tocTitle" to "Manual map",
        "tocAriaLabel" to "Manual navigation",
        "snapshotSlotLabel" to "Snapshot slot",
        "toc" to d(
            "overview" to "Overview",
            "authFlow" to "Authentication flow",
            "userRegistration" to "Automatic user registration",
            "passwordRegistration" to "Automatic password registration",
            "login" to "Login",
            "attendance" to "Check-in and check-out",
            "projectSelection" to "Project selection",
            "location" to "Location permission",
            "automaticActivities" to "Automatic activities",
            "transport" to "Transport",
            "passwordChange" to "Password reset/change",
            "settings" to "Settings",
            "support" to "Support",
            "faq" to "Common problems and FAQ",
        ),
        "sections" to d(
            "overview" to d(
                "title" to "Overview",
                "lead" to "Checking Web combines authentication, attendance registration, location context, and transport access in a single mobile-first surface.",
                "item1" to "The main screen shows the key, password, recent history, attendance form, and the shortcut to Transport.",
                "item2" to "Assistance flows appear automatically when the key does not exist yet or when the user still needs to create the first password.",
                "item3" to "The Settings menu groups secondary actions so the main area stays compact.",
                "figureCaption" to "Checking Web main shell with the authentication area and attendance form.",
            ),
            "authFlow" to d(
                "title" to "Authentication flow",
                "lead" to "The application checks the key status as soon as it reaches four valid characters and decides which assistance flow to show.",
                "item1" to "If the key does not exist, the flow moves into user registration without waiting for another click.",
                "item2" to "If the key exists but still has no password, the interface opens password registration directly.",
                "item3" to "If the key and password already exist, the interface stays on the normal login path.",
                "note" to "Closing a modal manually does not create an endless loop: the system only tries again when the key or the relevant auth state really changes.",
            ),
            "userRegistration" to d(
                "title" to "Automatic user registration",
                "lead" to "When the key does not exist in the database, the app opens the self-service form so a new user can complete registration.",
                "item1" to "The form asks for key, full name, projects, optional email, password, and password confirmation.",
                "item2" to "Projects are loaded from the API and the user can mark one or more valid items.",
                "item3" to "After a successful submission, the web session is authenticated and the app stays unlocked for use.",
                "figureCaption" to "New-user registration modal opened automatically for an unknown key.",
            ),
            "passwordRegistration" to d(
                "title" to "Automatic password registration",
                "lead" to "If the key already exists but no password is registered yet, Checking Web enters first-password creation mode.",
                "item1" to "The modal reuses the existing password component, but hides the old-password field.",
                "item2" to "The user enters the new password, confirms the value, and completes the first access.",
                "item3" to "After saving, the backend authenticates the web session and the main screen accepts the remaining operations.",
                "figureCaption" to "First-password creation modal for a user that already exists in the system.",
            ),
            "login" to d(
                "title" to "Login",
                "lead" to "Users who already exist type the key, enter the password, and let the app validate authentication before recording attendance or opening Transport.",
                "item1" to "The password can still be validated automatically by the existing main-shell flow.",
                "item2" to "If the user changes the typed password after authenticating, the app protects itself again until the new value is verified.",
                "item3" to "Status messages help distinguish \"waiting for password\", \"verifying\", and \"authenticated\".",
            ),
            "attendance" to d(
                "title" to "Check-in and check-out",
                "lead" to "With valid authentication, the user selects the registration type, reviews the context, and submits the operation from the main screen.",
                "item1" to "The form distinguishes check-in from check-out and can block manual submission when automatic activities are controlling the flow.",
                "item2" to "Visible history near the top helps confirm the last recorded event before sending a new one.",
                "item3" to "Success or failure messages appear in the status area after each attempt.",
                "figureCaption" to "Example of a successful attendance registration state.",
            ),
            "projectSelection" to d(
                "title" to "Project selection",
                "lead" to "The app uses projects both during initial registration and in the daily workflow to limit user context and available locations.",
                "item1" to "During registration, the user marks one or more valid projects before completing account creation.",
                "item2" to "After authentication, the main panel shows current projects and allows updates when that capability is enabled.",
                "item3" to "The active project influences location lists, contextual history, and other user-scoped surfaces.",
                "figureCaption" to "Example of the projects block in the authenticated shell, showing the active scope summary.",
            ),
            "location" to d(
                "title" to "Location permission and GPS behavior",
                "lead" to "The device position is used to determine operational context, trigger automatic flows, and guide the user when accuracy is not yet good enough.",
                "item1" to "The application depends on HTTPS, browser support, and active permission to request precise location.",
                "item2" to "When permission is denied or unavailable, the app shows clear messages and can limit the flow to the allowed manual fallback.",
                "item3" to "When location is obtained successfully, the interface updates accuracy, recognized place, and related automatic actions.",
                "figureCaptionDenied" to "Example of the screen when location permission is still unavailable.",
                "figureCaptionGranted" to "Example of the screen after precise location access is granted successfully.",
            ),
            "automaticActivities" to d(
                "title" to "Automatic activities",
                "lead" to "The app can also trigger check-in, check-out, and related updates when location context changes in the expected way.",
                "item1" to "This mechanism depends on valid authentication, location readings, and internal rules that avoid unsafe transitions.",
                "item2" to "When automatic activities are controlling the flow, some manual fields become hidden or disabled to protect record consistency.",
                "item3" to "If precision drops below the required threshold, the app may re-enable manual fallback options instead of making a risky assumption.",
            ),
            "transport" to d(
                "title" to "Transport access",
                "lead" to "After authentication, the user can open the Transport module to request rides, inspect status, and review the most recent request details.",
                "item1" to "Access stays protected by the same key and password validated in the main shell.",
                "item2" to "The module includes address registration, per-type requests, and a summary of the current status.",
                "item3" to "When a vehicle is allocated, the screen shows the main operational details for the user.",
                "figureCaption" to "Example of the Transport surface opened from the Checking Web shell.",
            ),
            "passwordChange" to d(
                "title" to "Reset or change password",
                "lead" to "Password change is no longer placed in the main authentication row and now lives inside Settings.",
                "item1" to "Open Settings, tap Change Password, and use the existing password-change modal.",
                "item2" to "The flow asks for old password, new password, and confirmation while preserving the current validations.",
                "item3" to "The action stays enabled only when the user is authenticated and already has a registered password.",
                "figureCaption" to "Password-change flow opened from Settings > Change Password.",
            ),
            "settings" to d(
                "title" to "Settings",
                "lead" to "The gear icon opens a central place for preferences and secondary actions without crowding the authentication row.",
                "item1" to "Language updates the visible labels of the main application and keeps the preference in browser storage.",
                "item2" to "Allow Location reuses the existing pipeline to request precise access again when that is still needed.",
                "item3" to "Support and About use the same entry point to open WhatsApp and this documentation.",
                "figureCaption" to "Settings widget with the new centralized secondary actions.",
            ),
            "support" to d(
                "title" to "Support",
                "lead" to "When the user needs human help, Settings > Support prepares a WhatsApp conversation with the user key already included in the first message.",
                "item1" to "The link uses the official phone number configured in the Checking Web frontend.",
                "item2" to "The initial message is prepared automatically to reduce friction and speed up support.",
                "item3" to "If no valid key is available, the support button stays disabled for safety.",
            ),
            "faq" to d(
                "title" to "Common problems and FAQ",
                "lead" to "Use these quick answers when the interface feels stuck or when the flow is not progressing as expected.",
                "q1" to "Why did the app open a registration flow by itself?",
                "a1" to "This happens when the key does not exist or when the account still has no password. The new flow removes extra clicks and takes the user straight to the correct action.",
                "q2" to "Why is the Allow Location button disabled?",
                "a2" to "Because the application already understands that precise permission is active, or because a location refresh is already running.",
                "q3" to "What should I do if Transport does not open?",
                "a3" to "First confirm that the key and password were authenticated. If the problem continues, open Support so your key can be sent to the support team.",
            ),
        ),
        "figures" to d(
            "authShellAlt" to "Checking Web main shell with key and password fields.",
            "userRegistrationAlt" to "New-user registration form in Checking Web.",
            "passwordRegistrationAlt" to "Initial password-registration modal for an existing user without password.",
            "settingsModalAlt" to "Checking Web Settings widget open with language, password, location, support, and about actions.",
            "passwordChangeAlt" to "Password-change modal opened from Settings.",
            "locationDeniedAlt" to "Checking Web state with location permission denied or unavailable.",
            "locationGrantedAlt" to "Checking Web state with precise location available and shared.",
            "projectSelectionAlt" to "Project-selection area in Checking Web.",
            "transportScreenAlt" to "Transport module screen inside the Checking Web ecosystem.",
            "checkSuccessAlt" to "Successful check-in or check-out state in Checking Web.",
        ),
    ),
    "accident" to d(
        "button" to d(
            "report" to "Report Accident",
            "reported" to "Accident Reported",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "I need help with the Web application. My key is {chave}.",
    ),
    "autoActivities" to d(
        "title" to "Automatic Activities",
        "subtitle" to "Enable background automatic check-in/check-out",
        "enable" to "Enable Automatic Activities",
        "insufficientPermissions" to "Minimum permissions not granted. In Settings › Permissions, grant Notifications and Precise Location to enable automatic activities.",
        "reducedReliability" to "Automatic activities are on. For reliable background operation, grant Location as 'Allow all the time' and disable battery optimization in Settings › Permissions.",
        "permNotifications" to "Notifications",
        "permLocationAllTime" to "Location 'all the time'",
        "permBattery" to "Battery unrestricted",
        "permAutoStart" to "Start with device",
        "nudgeQuestion" to "Want Checking to check you in and out automatically, based on your location?",
        "nudgeActivate" to "Activate now",
        "nudgeLater" to "Not now",
        "permStep" to d(
            "location" to "Location permission",
            "background" to "Background location (\"Allow all the time\")",
            "batteryOpt" to "Battery optimization exemption",
            "notification" to "Notification permission",
            "oem" to "Manufacturer auto-start settings",
        ),
        "notification" to d(
            "channelName" to "Automatic Activities",
            "channelDesc" to "Background check-in/check-out status and alerts",
            "serviceTitle" to "Checking — Automatic Activities",
            "serviceBody" to "Monitoring your location in the background.",
            "servicePaused" to "Automatic activities paused.",
            "eventTitle" to "Checking — {action}",
            "eventBody" to "{local} • {hora}",
            "brandTitle" to "Checking",
            "checkinMessage" to "Check-In done.",
            "checkoutMessage" to "Check-Out done.",
            "pauseStartMessage" to "Checking paused.",
            "pauseEndMessage" to "Checking active.",
            "accidentMessage" to "Checking: accident reported!",
            "reauthTitle" to "Checking — Re-authentication required",
            "reauthBody" to "Open the app to sign in again.",
        ),
    ),
    "scheduledPause" to d(
        "buttonLabel" to "Scheduled Pause",
        "title" to "Scheduled Pause",
        "explanation" to "The scheduled pause saves battery by completely suspending automatic Checking updates during the specified period. While the pause is active, the app performs no activity — neither by proximity to a location nor by periodic check — resuming automatic activities only at the end of the period.",
        "enable" to "Enable scheduled pause.",
        "from" to "From:",
        "to" to "To:",
        "suspendSaturdays" to "Suspend on Saturdays.",
        "suspendSundays" to "Suspend on Sundays.",
        "close" to "Close",
        "notificationPaused" to "Scheduled pause active",
    ),
    "instructions" to d(
        "heading" to "Instructions",
        "intro" to "This guide walks you through using Checking: recording attendance manually, enabling Automatic Mode (location-based check-in/check-out), and setting up the Scheduled Pause.",
        "step1" to d(
            "title" to "1. Sign in",
            "item1" to "On the home screen, type your 4-character key in the 'Key' field. It glows orange when the key is found.",
            "item2" to "Type your password in the 'Password' field. On success the fields turn green and 'Authentication completed' appears.",
            "item3" to "If you don't have a password yet, the app opens password setup automatically; if the key doesn't exist, it offers self-registration.",
        ),
        "step2" to d(
            "title" to "2. Record attendance manually",
            "item1" to "Pick 'Check-In' or 'Check-Out' and the type 'Normal' or 'Backdated'.",
            "item2" to "With Automatic Mode off, select the 'Location' from the list and tap 'Register Check-In' (or 'Check-Out').",
            "item3" to "The card at the top shows your last check-in and check-out; tap it to see the full list with date, time and location.",
        ),
        "step3" to d(
            "title" to "3. Enable Automatic Mode",
            "lead" to "With Automatic Mode, the app checks you in and out on its own, based on your location — when you enter or leave a registered area, when you foreground the app, and on periodic checks.",
            "item1" to "Tap the gear (next to the key/password fields) to open 'Settings'.",
            "item2" to "Tap 'Automatic Activities' and tick the 'Enable Automatic Activities' box.",
            "item3" to "Grant each permission in the list by tapping it: Notifications, Location 'all the time' (Allow always), Battery unrestricted and — on some devices — 'Start with device'.",
            "item4" to "When the gear glows GREEN, Automatic Mode is active and healthy. An ORANGE glow means a recommended permission is missing.",
            "callout" to "Important: for reliable background operation, grant Location as 'Allow all the time' and disable battery optimization for Checking.",
        ),
        "step4" to d(
            "title" to "4. Enable the Scheduled Pause",
            "lead" to "The Scheduled Pause saves battery by suspending automatic activities during a period (for example, overnight).",
            "item1" to "In 'Settings', tap 'Scheduled Pause'.",
            "item2" to "Turn it on and set the 'From' and 'To' times (for example, 22:00 to 06:00).",
            "item3" to "Optionally, also tick 'Suspend on Saturdays' and/or 'Suspend on Sundays'.",
            "item4" to "During the pause the app performs no automatic activity; it resumes on its own at the end of the period.",
        ),
        "step5" to d(
            "title" to "5. Track your history",
            "item1" to "Tap 'LAST CHECK-IN' or 'LAST CHECK-OUT' to open the table with the Date, Time and Location of each record.",
            "item2" to "Records made near but outside a registered area show as 'Localização não Cadastrada' (unregistered location).",
            "item3" to "Even with no internet, your records are saved on the device and sent as soon as the connection returns, always with the original time.",
        ),
        "step6" to d(
            "title" to "6. Request transport",
            "item1" to "Tap 'Transport' to open the personnel transport module.",
            "item2" to "Enter the address and time you need and submit the request.",
            "item3" to "The logistics manager organizes the trips; an artificial-intelligence engine suggests how to group passengers and order the stops.",
        ),
        "step7" to d(
            "title" to "7. In case of an accident",
            "lead" to "Accident Mode is a safety feature. Use it only in a real emergency.",
            "item1" to "Any user can open Accident Mode; it notifies, in real time, every user on the same project.",
            "item2" to "Report your situation and zone: 'safe', 'at the accident site but safe', or 'at the accident site and needing help'.",
            "item3" to "If possible, record a video of the site: it is sent in real time to the administrator's dashboard.",
            "item4" to "The 'Call Emergency Service' button calls the local emergency service, reporting the accident and location in the region's language.",
        ),
        "step8" to d(
            "title" to "8. Other settings",
            "item1" to "'Alerts': choose which notifications you receive (activities, scheduled pause, accident).",
            "item2" to "'Language': switch the app language.",
            "item3" to "'Change Password': set a new password whenever needed.",
            "item4" to "'Support': talk to the team directly on WhatsApp.",
            "item5" to "'About': learn the story of Checking and the parts that make up the system.",
        ),
        "closing" to "That's it! With Automatic Mode on, you don't need to record anything manually — Checking takes care of it for you.",
    ),
    "about" to d(
        "heading" to "About Checking",
        "introTitle" to "How Checking came to be",
        "introBody" to "Checking began development in March 2026, conceived by Engineer Dilnei Schmidt.\n" +
            "\n" +
            "There was a need to quickly identify every Petrobras employee present at the construction and assembly worksite, in case an accident occurred.\n" +
            "\n" +
            "The HSE management's first solution was an online form, filled in by each employee on arrival at and departure from the worksite. It worked for identifying who was present, but it was laborious and many people would occasionally forget to fill it in.\n" +
            "\n" +
            "To improve efficiency, Dilnei built an app able to:\n" +
            "• identify, by GPS, how close the user was to the worksite and prompt them to check in;\n" +
            "• preset alarms at typical check-in and check-out times, reminding the user to fill in the form;\n" +
            "• automatically fill in the form with the user's data and submit it online.\n" +
            "\n" +
            "This made the work easier and increased how often the form was filled in.\n" +
            "\n" +
            "Still in March 2026, Engineer Tamer Salmem learned of the solutions in place and advanced the use of current programming technologies, developing the system first conceived by Dilnei.\n" +
            "\n" +
            "The goal was for users not to have to worry about opening an app to check in or out. It also meant building real-time tracking so administrators would know not only who was at work, but in which of each project's registered locations every user was — increasing the ability to respond in emergencies.\n" +
            "\n" +
            "So the system gained:\n" +
            "• service activation by geofencing (based on the user's proximity to the worksite);\n" +
            "• background task execution — checking in at each location change inside the facilities and checking out when the user moves away, without even unlocking the device;\n" +
            "• real-time delivery of users' location to the administrator's dashboard;\n" +
            "• the ability to register as many projects as needed, anywhere in the world.\n" +
            "\n" +
            "The system can also enter 'Accident Mode'. In an accident, any user can trigger an alarm that notifies, in real time, every user on the same project. With Accident Mode active:\n" +
            "• a table is created on the administrator's dashboard, listing each user's status: 'safe', 'at the accident site but safe', and 'at the accident site and needing help';\n" +
            "• the user can record a video and send it in real time, as a link in the table, so the administrator sees footage of the site;\n" +
            "• the 'Call Emergency Service' button calls the registered local emergency service, reporting the accident, the location and the contact person, speaking the region's language.\n" +
            "\n" +
            "The system's robustness and reliability brought operational safety and immediate response to the Petrobras HSE team.\n" +
            "\n" +
            "Finally, Engineer Thiago Soares do Nascimento integrated the information the system produces into the existing management dashboards, so the new system works alongside the old form-filling, keeping management controls up to date.\n" +
            "\n" +
            "That is how CHECKING was born.",
        "partsTitle" to "The parts of the system",
        "partsIntro" to "Checking is an attendance-control system that records employees' entry to and exit from worksites. It works through different channels — RFID card readers installed on site, an Android app, a web page accessible from the phone, and an administration panel — bringing everything together in one place.\n" +
            "\n" +
            "It is made up of:\n" +
            "• an API, built in Python/FastAPI;\n" +
            "• a website for the system administrators;\n" +
            "• a Web application, responsive for phones and desktops;\n" +
            "• a dashboard for personnel transport;\n" +
            "• an Android-only app, built in Kotlin.",
        "partApiTitle" to "API",
        "partApiBody" to "The API is the brain of the system. Whenever someone checks in or out — via the physical reader, the app, or the web page — it receives the information, verifies it is correct, saves it to the database, and notifies the other components in real time.\n" +
            "\n" +
            "It also automatically fills in the corporate Microsoft Forms after each record, coordinates the transport system, triggers emergency alerts in case of an accident, and ensures no data is lost when the connection is unstable.",
        "partWebsiteTitle" to "Website",
        "partWebsiteBody" to "The website is the administrators' control panel. Through it you can see in real time who is checked in and who is checked out, and manage every aspect of the system without technical knowledge.\n" +
            "\n" +
            "Main features: register and edit employees, create projects and their rules, define the geographic areas the system recognizes, view attendance reports, and export data. It is also the central point to trigger and follow Accident Mode — seeing each employee's status in real time and coordinating the emergency response.",
        "partWebappTitle" to "Web application",
        "partWebappBody" to "The web application is the employees' tool. It runs in the phone's or computer's browser, with nothing to install, and lets each person record entry or exit, view their history, and request transport.\n" +
            "\n" +
            "When the employee turns on Automatic Activities, the phone itself detects the location and checks in or out automatically when entering or leaving the registered areas. In an accident, the interface changes and asks the employee to report their situation and safety zone.\n" +
            "\n" +
            "It is available in six languages (Portuguese, English, Chinese, Malay, Indonesian, and Tagalog) to serve international teams.",
        "partTransportTitle" to "Transport dashboard",
        "partTransportBody" to "The transport dashboard is the tool for whoever handles travel logistics. Through it you can register the vehicles, view and organize the transport requests made by employees, and assign each person to a vehicle for the day.\n" +
            "\n" +
            "It includes an artificial-intelligence engine that analyzes addresses and times and automatically suggests how to group passengers and order the stops in an optimized way — reducing travel time and the number of trips. The manager can accept the suggestion, adjust it, or build the assignment manually.",
        "partAndroidTitle" to "Android app",
        "partAndroidBody" to "The Android app offers the same features as the web application, with a more complete day-to-day experience. Its main advantage is geolocation automation: the app runs in the background and records check-in or check-out automatically as the employee enters and leaves the registered areas, without relying on the browser.\n" +
            "\n" +
            "It also works without internet: with no connection, records are saved on the phone and sent as soon as the connection returns, always with the original time. It also includes the history with the date, time, and location of each event, the transport module, and the emergency mode for accidents.",
        "rulesTitle" to "Check-in and check-out situations",
        "rulesIntro" to "The situations below describe, step by step, what the system must do for each user (check-in or check-out) in each typical scenario. The Web Application and the Native App follow the rules of their respective blocks.",
        "rulesWebTitle" to "Situations — Web Application",
        "rulesWebBody" to "## Situation 1 — Check-out on moving away\n" +
            "• Automatic Activities on, with full location permission.\n" +
            "• The last activity was a check-in.\n" +
            "• The app refreshes the location and sees the user is in the 'Check-Out Zone' or more than 2 km from any registered place (except the Check-Out Zone).\n" +
            "• Since the last activity was a check-in, the app checks the user out.\n" +
            "\n" +
            "## Situation 2 — Already checked out, far away or in the Check-Out Zone\n" +
            "• The last activity was a check-out.\n" +
            "• The user is in the 'Check-Out Zone' or more than 2 km from any registered place.\n" +
            "• No action: a check-out is not repeated because of a location change.\n" +
            "\n" +
            "## Situation 3 — Arriving at work (check-in)\n" +
            "• The last activity was a check-out.\n" +
            "• The user is INSIDE a registered area other than the 'Check-Out Zone' (an actual match with the area, not mere proximity).\n" +
            "• The user is effectively at work (including the first check-in of the day).\n" +
            "• The app checks the user in and updates the location to the matching registered area.\n" +
            "! IMPORTANT: if the user is NOT inside any registered area — even if nearby (under 2 km from some coordinate, excluding the Check-Out Zone) — the app does NOT check in automatically; it only shows 'Localização não Cadastrada' (same as Situation 5).\n" +
            "\n" +
            "## Situation 4 — New check-in (always)\n" +
            "• The last activity was a check-in.\n" +
            "• The user is in a registered area other than the 'Check-Out Zone'.\n" +
            "• The app performs a new check-in REGARDLESS of whether the location changed.\n" +
            "• Even at the SAME place as the last check-in, a new check-in is made to record/update the location and time.\n" +
            "\n" +
            "## Situation 5 — Nearby but outside any area\n" +
            "• The last activity was a check-in.\n" +
            "• The user is not in any registered area, but also not more than 2 km from some registered coordinate (excluding the Check-Out Zone). That is, they are near work.\n" +
            "• No action: the app only shows 'Localização não Cadastrada'.\n" +
            "\n" +
            "## Situation 6 — 'Refresh' button after a check-in\n" +
            "• The app is already in the foreground; the last activity was a check-in.\n" +
            "• The user taps 'Refresh' to update the location.\n" +
            "• The app performs a new check-in REGARDLESS of whether the location changed, to record/update the location and time.\n" +
            "\n" +
            "## Situation 7 — Leaving the Check-Out Zone\n" +
            "• In the foreground; the last activity was a check-out; the user is in the 'Check-Out Zone' (no action).\n" +
            "• The user taps 'Refresh' and the app sees they left the Check-Out Zone, to:\n" +
            "• Variant 7A — a registered area other than the 'Check-Out Zone';\n" +
            "• Variant 7B — no registered area, but still nearby (under 2 km, excluding the Check-Out Zone).\n" +
            "• In both, the app immediately checks in, updating the location to the registered area or, with no exact match, to 'Localização não Cadastrada'.\n" +
            "\n" +
            "## Situation 8 — Mixed Zone\n" +
            "• The app detects the position matches the 'Mixed Zone' (on first entry or a consecutive reading).\n" +
            "• If the last relevant activity was NOT in the 'Mixed Zone' itself, the switch is immediate: 8A — after a check-in → check-out in the 'Mixed Zone'; 8B — after a check-out → check-in in the 'Mixed Zone'.\n" +
            "• The 'Mixed Zone Time Interval' field (admin website 'Registration' tab) is the cooldown for consecutive readings in the Mixed Zone only: while elapsed_time < interval, a new switch is blocked; when >= interval, it is allowed again.\n" +
            "• Exception after a check-in in the Mixed Zone: going to the 'Check-Out Zone' or beyond the 'Minimum distance for automatic check-out' → immediate check-out, without waiting for the cooldown.\n" +
            "• Exception after a check-out in the Mixed Zone: going to another registered area (except Check-Out Zone and Mixed Zone) or staying within the minimum distance → immediate check-in, discarding the cooldown.\n" +
            "\n" +
            "## Situation 9 — Manual mode (Automatic Activities off)\n" +
            "• The user is authenticated; Automatic Activities are OFF.\n" +
            "• The app updates the location if there is permission; otherwise it shows 'Permission denied'.\n" +
            "• The user chooses 'check-in' or 'check-out', 'Normal' or 'Retroactive', selects the 'Location' (available whenever Automatic Activities are off), and taps 'Submit'.\n" +
            "• The app follows the normal flow and performs the activity per the selections.",
        "rulesNativeTitle" to "Situations — Native App (Android)",
        "rulesNativeBody" to "## Situation 1 — Check-out on moving away\n" +
            "• The last activity was a check-in.\n" +
            "• The user is in the 'Check-Out Zone' or more than 2 km from any registered place (except the Check-Out Zone).\n" +
            "• The app checks the user out.\n" +
            "\n" +
            "## Situation 2 — Already checked out, far away or in the Check-Out Zone\n" +
            "• The last activity was a check-out.\n" +
            "• The user is in the 'Check-Out Zone' or more than 2 km from any registered place.\n" +
            "• No action: a check-out is not repeated because of a location change.\n" +
            "\n" +
            "## Situation 3 — Arriving at work (check-in)\n" +
            "• The last activity was a check-out.\n" +
            "• The user is INSIDE a registered area other than the 'Check-Out Zone' (an actual match, not mere proximity).\n" +
            "• The app checks the user in and updates the location to the matching area.\n" +
            "! IMPORTANT: starting from a CHECK-OUT, if the user is NOT inside any registered area — even if nearby — the app does NOT check in; it only shows 'Localização não Cadastrada' (see Variant 7B). When the last activity was a CHECK-IN and the user is nearby but outside any area, the behavior is different: the app makes a check-in with 'Localização não Cadastrada' as a change (see Situation 5).\n" +
            "\n" +
            "## Situation 4 — New check-in only on a location change\n" +
            "• The last activity was a check-in.\n" +
            "• The user is in a registered area other than the 'Check-Out Zone'.\n" +
            "• The app performs a new check-in ONLY if the area is DIFFERENT from the last check-in's.\n" +
            "• At the SAME place as the last check-in, NO action (this eliminates the duplicate check-in). On changing area, the new check-in records/updates the location and time.\n" +
            "\n" +
            "## Situation 5 — Nearby but outside any area (continuation)\n" +
            "• The last activity was a check-in.\n" +
            "• The user is not in any registered area, but is nearby (under 2 km from some coordinate, excluding the Check-Out Zone).\n" +
            "• Since they left the area, the app makes a check-in with 'Localização não Cadastrada', recording the continuity of the journey.\n" +
            "• It only happens as a CHANGE: if the last check-in was already 'Localização não Cadastrada', NO action (it does not repeat).\n" +
            "\n" +
            "## Situation 6 — 'Refresh' button after a check-in\n" +
            "• In the foreground; the last activity was a check-in.\n" +
            "• The user taps 'Refresh'.\n" +
            "• A new check-in ONLY if the location is DIFFERENT from the last check-in's (same rule as Situation 4). At the SAME place, NO action.\n" +
            "\n" +
            "## Situation 7 — Leaving the Check-Out Zone\n" +
            "• In the foreground; the last activity was a check-out; the user is in the 'Check-Out Zone' (no action).\n" +
            "• The user taps 'Refresh' and the app updates the location to: Variant 7A — a registered area other than the 'Check-Out Zone'; Variant 7B — no registered area, but still nearby (under 2 km, excluding the Check-Out Zone).\n" +
            "• 7A: since the last activity was a check-out, the app immediately checks in at the matching area.\n" +
            "• 7B: since the user is checked out and is NOT inside any area, the app does NOT check in; it only shows 'Localização não Cadastrada' (same rule as the Situation 3 note).\n" +
            "\n" +
            "## Situation 8 — Mixed Zone\n" +
            "• The app detects the 'Mixed Zone' and, if the last relevant activity was not in it, switches immediately: 8A — after a check-in → check-out in the 'Mixed Zone'; 8B — after a check-out → check-in in the 'Mixed Zone'.\n" +
            "• The 'Mixed Zone Time Interval' is the cooldown for consecutive readings in the Mixed Zone only: while elapsed_time < interval, a new switch is blocked; when >= interval, it is allowed again.\n" +
            "• Immediate exceptions (discarding the cooldown): going to the 'Check-Out Zone' or beyond the minimum distance → check-out; going to another registered area or staying within the minimum distance → check-in.\n" +
            "\n" +
            "## Situation 9 — Manual mode (Automatic Activities off)\n" +
            "• Authenticated user; Automatic Activities OFF.\n" +
            "• The app updates the location if there is permission; otherwise it shows 'Permission denied'.\n" +
            "• The user chooses check-in/check-out, Normal/Retroactive, selects the 'Location', and taps 'Submit'.\n" +
            "• The app follows the normal flow per the selections.",
        "notesTitle" to "General notes",
        "notesBody" to "## Foreground trigger\n" +
            "• Opening the app or bringing it to the foreground, with Automatic Activities on and the user authenticated, triggers the automatic evaluation (the engine decides check-in or check-out per the situations). The same applies to geofencing and to the periodic check every 15 minutes.\n" +
            "• There is no 'blind' periodic check-in: the 15-minute check always verifies the location and keeps the 'skip if nothing changed' rule.\n" +
            "\n" +
            "## Check-in only on a location change\n" +
            "• An automatic check-in happens only when the resolved location is DIFFERENT from the last check-in's. Same location → no action. This rule (Situations 4 and 6) is what ELIMINATES the duplicate check-in.\n" +
            "\n" +
            "## FORMS per project\n" +
            "• On the first check-in of the day and on every check-out, the form is filled in and submitted ONCE PER PROJECT the user is registered in (respecting each project's 'forms enabled'). E.g., a user in projects P80 and P83 → two submissions. A single-project user → one submission.\n" +
            "\n" +
            "## Check-out invariants (preserved)\n" +
            "• Automatic check-out happens in all the cases described (Check-Out Zone, distance beyond the limit, Mixed Zone switch); there are never two consecutive check-outs; after a check-out, the next automatic activity is always a check-in.",
    ),
)

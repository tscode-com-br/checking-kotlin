package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun tlDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Manwal ng Checking",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "Form para sa check-in at check-out",
        "credentialsAria" to "Pagkakakilanlan ng user at password",
        "keyLabel" to "Susi",
        "passwordLabel" to "Password",
        "keyPlaceholder" to "Hal.: HR70",
        "passwordPlaceholder" to "3 hanggang 10 character",
        "requestRegistrationButton" to "Humiling ng rehistro",
        "awaitingApproval" to "Naghihintay ng pag-apruba ng rehistro.",
        "registrationQueueFull" to "Puno na ang pila ng rehistro. Mangyaring ipaalam sa administrator ng sistema.",
        "settingsSpacer" to "Settings",
        "openSettingsAria" to "Buksan ang settings",
        "openSettingsTitle" to "Buksan ang settings",
        "waitingAuthentication" to "Naghihintay ng pagpapatunay.",
        "enterPasswordPrompt" to "Ilagay ang password para magsimula.",
        "createPasswordPrompt" to "Ilagay ang susi at gumawa ng password.",
        "invalidFourCharacterKey" to "Maglagay ng 4-character na alphanumeric na susi.",
        "unknownUserDetail" to "Hindi rehistrado ang susi ng user",
        "transportAccessPrompt" to "Ilagay ang susi at i-validate ang password para ma-access ang Transport.",
    ),
    "history" to d(
        "lastCheckinLabel" to "Huling Check-In",
        "lastCheckoutLabel" to "Huling Check-Out",
        "today" to "Ngayon",
        "yesterday" to "Kahapon",
        "dialogTitleCheckin" to "Kasaysayan ng Check-In",
        "dialogTitleCheckout" to "Kasaysayan ng Check-Out",
        "colDate" to "Petsa",
        "colTime" to "Oras",
        "colLocal" to "Lokasyon",
        "empty" to "Walang nahanap na record.",
        "back" to "Bumalik",
        "loadingMessage" to "Sinusuri ang history...",
        "notFoundMessage" to "Walang nahanap na record para sa susi na ito.",
        "noRecordsMessage" to "Walang check-in o check-out na naitala para sa susi na ito.",
        "updatedMessage" to "Na-update na ang history para sa inilagay na susi.",
        "loadFailed" to "Hindi masuri ang history para sa susi na ito.",
        "colActivity" to "Aktibidad",
        "activityCheckin" to "Check-In",
        "activityCheckout" to "Check-Out",
        "loadError" to "Hindi ma-load ang history.",
        "retry" to "Subukang muli",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "Mga Awtomatikong Aktibidad",
        "sectionTitle" to "Pagrehistro",
        "checkinLabel" to "Check-In",
        "checkoutLabel" to "Check-Out",
        "transportLabel" to "Transportasyon",
        "informeTitle" to "Uri",
        "informeNormalLabel" to "Normal",
        "informeRetroativoLabel" to "Retroaktibo",
        "submitButton" to "Irehistro",
        "checkInLowerLabel" to "check-in",
        "checkOutLowerLabel" to "check-out",
        "disableAutomaticActivitiesForManualSubmit" to "I-disable ang Mga Awtomatikong Aktibidad para makapagrehistro nang mano-mano.",
        "selectLocationBeforeSubmit" to "Pumili ng lokasyon bago magrehistro.",
    ),
    "settings" to d(
        "title" to "Settings",
        "languageLabel" to "Wika",
        "resetPasswordLabel" to "Palitan ang Password",
        "allowLocationLabel" to "Payagan ang Lokasyon",
        "allowAudioVideoLabel" to "Payagan ang Audio at Video",
        "supportLabel" to "Suporta",
        "manualLabel" to "Mga Tagubilin sa Paggamit",
        "aboutLabel" to "Tungkol Dito",
        "activitiesLabel" to "Mga Aktibidad",
        "backButton" to "Bumalik",
    ),
    "passwordDialog" to d(
        "titleChange" to "Palitan ang Password",
        "titleRegister" to "Gumawa ng Password",
        "oldPasswordLabel" to "Lumang Password",
        "newPasswordLabel" to "Bagong Password",
        "confirmPasswordLabel" to "Kumpirmahin ang Password",
        "backButton" to "Bumalik",
        "submitChangeButton" to "Palitan",
        "submitRegisterButton" to "I-save",
        "changingStatus" to "Pinapalitan ang password...",
        "savingStatus" to "Sine-save ang password...",
        "validatingStatus" to "Sine-verify ang password.",
        "oldPasswordInvalid" to "Ang lumang password ay dapat nasa pagitan ng 3 at 10 character.",
        "newPasswordInvalid" to "Ang bagong password ay dapat nasa pagitan ng 3 at 10 character.",
        "confirmMismatch" to "Hindi tugma ang kumpirmasyon ng bagong password.",
        "changeFailed" to "Hindi mapalitan ang password.",
        "validationFailed" to "Hindi ma-validate ang password.",
        "statusLoadFailed" to "Hindi masuri ang status ng password.",
    ),
    "registrationDialog" to d(
        "title" to "Humiling ng Rehistro",
        "note" to "Punan ang impormasyong nasa ibaba para magamit ang Checking system.",
        "keyLabel" to "Susi",
        "fullNameLabel" to "Buong Pangalan",
        "projectsLabel" to "Mga Proyekto",
        "projectsHint" to "Pumili ng isa o higit pang proyekto.",
        "emailLabel" to "Email",
        "emailPlaceholder" to "Opsyonal",
        "passwordLabel" to "Password",
        "confirmPasswordLabel" to "Kumpirmahin ang Password",
        "backButton" to "Bumalik",
        "submitButton" to "Ipadala",
        "loadingProjects" to "Nilo-load ang mga proyekto...",
        "noProjectsAvailable" to "Walang available na proyekto sa ngayon.",
        "fullNameRequired" to "Ilagay ang buong pangalan.",
        "emailInvalid" to "Maglagay ng wastong email o iwanang blangko ang field.",
        "passwordInvalid" to "Ang password ay dapat nasa pagitan ng 3 at 10 character.",
        "confirmMismatch" to "Hindi tugma ang kumpirmasyon ng bagong password.",
        "submittingStatus" to "Ipinapadala ang hiling sa rehistro...",
        "successStatus" to "Matagumpay na nakumpleto ang rehistro.",
        "submitFailed" to "Hindi maipadala ang hiling sa rehistro.",
    ),
    "location" to d(
        "title" to "Lokasyon",
        "waitingLabel" to "Naghihintay ng lokasyon.",
        "refreshLabel" to "I-refresh ang lokasyon",
        "refreshBusyLabel" to "Nire-refresh ang lokasyon",
        "unavailableShort" to "Hindi Available",
        "unavailableLabel" to "Hindi available ang lokasyon",
        "unavailableMessage" to "Hindi masuri ang lokasyon sa ngayon.",
        "noPermissionLabel" to "Walang Pahintulot",
        "timeoutLabel" to "Naubos ang Oras",
        "timeoutMessage" to "Mas matagal kaysa inaasahan ang paghahanap ng lokasyon.",
        "detectingLabel" to "Tinutukoy...",
        "exactConfirmationBrowser" to "Naghihintay ng kumpirmasyon ng eksaktong lokasyon mula sa browser.",
        "exactConfirmationApp" to "Naghihintay ng kumpirmasyon ng eksaktong lokasyon mula sa shortcut/app.",
        "updatingDeviceLocation" to "Ina-update ang kasalukuyang lokasyon ng device.",
        "secureContextRequired" to "Ang eksaktong lokasyon ay nangangailangan ng secure na koneksyon (HTTPS).",
        "browserUnsupported" to "Hindi sinusuportahan ng browser na ito ang eksaktong lokasyon.",
        "permissionBlocked" to "Naka-block ang pahintulot sa lokasyon sa browser. Payagan ang site access sa settings ng browser.",
        "captureRequiresSupport" to "Ang pagkuha ng lokasyon ay nangangailangan ng HTTPS at suporta ng browser.",
        "noValidPosition" to "Hindi makakuha ng wastong posisyon ng device.",
        "searchingPrecision" to "Naghahanap ng sapat na precision...",
        "completionStatus" to "Tapos na ang pag-update ng lokasyon.",
        "completionStatusWithDetail" to "Tapos na ang pag-update ng lokasyon. {detail}",
        "browserContextLabel" to "sa browser na ito",
        "appContextLabel" to "sa shortcut/app na ito",
        "browserSourceLabel" to "sa pamamagitan ng browser",
        "appSourceLabel" to "sa pamamagitan ng shortcut/app",
        "currentAccuracyLabel" to "Kasalukuyang accuracy",
        "accuracyPrefix" to "Accuracy",
        "accuracyTemplate" to "Accuracy {accuracy}",
        "accuracyLimitTemplate" to "Limit {limit} m",
        "accuracyCombinedTemplate" to "Accuracy {accuracy} / Limit {limit} m",
        "noKnownLocations" to "Walang nakarehistrong lokasyon",
        "defaultManualLocationLabel" to "Pangunahing Opisina",
        "accuracyFallbackManualLocationLabel" to "Hindi Sapat ang Accuracy",
        "outsideWorkplaceLabel" to "Nasa Labas ng Lugar ng Trabaho",
        "unregisteredLocationLabel" to "Hindi Nakarehistrong Lokasyon",
        "mixedZoneLabel" to "Halo-halong Zone",
        "checkoutZoneLabel" to "Zone ng check-out",
    ),
    "projects" to d(
        "label" to "Mga Proyekto",
        "changeButton" to "Baguhin",
        "loadingProjects" to "Nilo-load ang mga proyekto...",
        "updatingProjects" to "Ina-update ang mga proyekto...",
        "noneAvailableShort" to "Walang available na proyekto",
        "noneAvailableSentence" to "Walang available na proyekto.",
        "noneAvailableNow" to "Walang available na proyekto sa ngayon.",
        "selectAtLeastOne" to "Pumili ng kahit isang proyekto.",
        "userProjectsAria" to "Mga proyekto ng user",
        "registrationProjectsAria" to "Mga proyekto ng rehistro",
        "updatedSuccess" to "Matagumpay na na-update ang mga proyekto.",
        "loadFailed" to "Hindi ma-load ang mga proyekto.",
        "userProjectsLoadFailed" to "Hindi ma-load ang mga proyekto ng user.",
        "updateFailed" to "Hindi ma-update ang mga proyekto.",
    ),
    "transport" to d(
        "title" to "Pag-iskedyul ng Transport",
        "backToMainAria" to "Bumalik sa pangunahing screen",
        "addressToggleLabel" to "Address:",
        "addressLabel" to "Address:",
        "zipLabel" to "ZIP Code:",
        "addressPlaceholder" to "Bloke (kung mayroon), kalye, at numero.",
        "zipPlaceholder" to "6 na digit lang",
        "addressBackButton" to "Bumalik",
        "addressSubmitButton" to "I-save",
        "optionInstruction" to "Piliin ang uri ng transport para magpatuloy.",
        "historyTitle" to "Mga aktibong request",
        "historyButtonLabel" to "Kasaysayan",
        "historyPanelTitle" to "Kasaysayan ng Request",
        "historyCloseButton" to "Isara",
        "kinds" to d(
            "regular" to "Araw ng Trabaho",
            "weekend" to "Weekend",
            "extra" to "Tiyak na Petsa",
        ),
        "statusLabels" to d(
            "available" to "Walang request",
            "pending" to "Nakabinbin",
            "confirmed" to "Nakumpirma",
            "realized" to "Natapos",
            "rejected" to "Tinanggihan",
            "cancelled" to "Kinansela",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "Lun",
                "1" to "Mar",
                "2" to "Miy",
                "3" to "Huw",
                "4" to "Biy",
                "5" to "Sab",
                "6" to "Lin",
            ),
            "full" to d(
                "0" to "Lunes",
                "1" to "Martes",
                "2" to "Miyerkules",
                "3" to "Huwebes",
                "4" to "Biyernes",
                "5" to "Sabado",
                "6" to "Linggo",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "Piliin ang mga araw:",
            "regularSubtitle" to "Piliin ang mga araw ng trabaho na gusto mo para sa request na ito.",
            "weekendSubtitle" to "Piliin ang mga araw ng weekend na gusto mo para sa request na ito.",
            "extraSubtitle" to "Suriin ang petsa at oras bago humiling.",
            "dateLabel" to "Petsa:",
            "timeLabel" to "Oras:",
            "backButton" to "Bumalik",
            "submitButton" to "Humiling",
            "requestUnavailable" to "Hindi available ang request sa transport.",
            "addressRequired" to "Magrehistro ng kumpletong address bago humiling ng transport.",
            "dateRequiredExtra" to "Ilagay ang petsa para sa extra transport.",
            "timeRequiredExtra" to "Ilagay ang oras para sa extra transport.",
            "dayRequired" to "Pumili ng kahit isang araw para humiling ng transport.",
            "conflictGeneric" to "Mayroon nang aktibong request sa transport para sa petsang iyon.",
            "conflictByDate" to "Mayroon nang aktibong request sa transport para sa {serviceDateLabel}.",
        ),
        "summary" to d(
            "noRequestRecorded" to "Walang naitalang request.",
            "noActiveRequests" to "Walang aktibong request.",
            "noRequestStatus" to "Walang request",
            "waitingAllocation" to "Naghihintay ng transport allocation.",
            "vehicleAllocated" to "Na-allocate na ang sasakyan.",
            "scheduleUnavailable" to "Hindi available ang iskedyul.",
            "requestClosed" to "Sarado na ang request.",
            "whenRequestExists" to "Kapag may request, lalabas ito rito.",
            "whenAllocated" to "Kapag naitalaga ka sa sasakyan, lalabas dito ang impormasyon.",
            "departureAndLimit" to "Aalis {departureTime} • Limit {deadlineTime}",
            "limitOnly" to "Limit {deadlineTime}",
        ),
        "detail" to d(
            "title" to "Detalye ng Request",
            "genericTitle" to "Transport",
            "waitingAllocation" to "Naghihintay ng transport allocation.",
            "whenAllocated" to "Kapag naitalaga ka sa sasakyan, lalabas dito ang impormasyon.",
            "inactive" to "Hindi na aktibo ang request na ito.",
            "confirmed" to "Nakumpirma ang transport.",
            "realized" to "Natapos ang transport.",
            "vehicleTypeLabel" to "Uri ng Sasakyan",
            "vehiclePlateLabel" to "Plate ng Sasakyan",
            "vehicleColorLabel" to "Kulay ng Sasakyan",
            "departureDateLabel" to "Petsa ng Pag-alis",
            "departureTimeLabel" to "Oras ng Pag-alis",
            "unavailableValue" to "Hindi available",
        ),
        "actions" to d(
            "markRealized" to "Natapos",
            "cancel" to "Kanselahin",
            "cancelling" to "Kinakansela...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "Maglagay ng wastong susi bago i-update ang address.",
            "invalidKeyBeforeRequest" to "Maglagay ng wastong susi bago humiling ng transport.",
            "requestFailed" to "Hindi makapag-request ng {requestLabel}.",
            "loadFailed" to "Hindi masuri ang transport.",
            "addressUpdated" to "Matagumpay na na-update ang address.",
            "addressUpdateFailed" to "Hindi ma-update ang address.",
            "cancelSuccess" to "Kinansela ang request sa transport.",
            "cancelFailed" to "Hindi makansela ang request.",
            "requestMarkedRealized" to "Namarkahan ang request bilang natapos.",
            "accessRequiresAuthentication" to "Ilagay ang susi at i-validate ang password para ma-access ang Transport.",
        ),
    ),
    "status" to d(
        "validationError" to "Error sa pag-validate.",
        "apiCommunicationFailure" to "Nabigo ang komunikasyon sa API.",
        "passwordVerifying" to "Sine-verify ang password.",
        "authenticationCompleted" to "Tapos na ang pagpapatunay. Ina-update ang application...",
        "userAuthenticated" to "Napatunayan ang user. Sinisimulan ang mga update.",
        "applicationUpdated" to "Matagumpay na na-update ang application.",
        "applicationUpdateFailed" to "Hindi ma-update ang application sa ngayon.",
        "checkinCompleted" to "Tapos na ang Check-In.",
        "checkoutCompleted" to "Tapos na ang Check-Out.",
        "savedOffline" to "Naka-save offline. Mag-sync ito kapag bumalik ang koneksyon.",
        "automaticCheckinCompleted" to "Tapos na ang awtomatikong Check-In.",
        "automaticCheckoutCompleted" to "Tapos na ang awtomatikong Check-Out.",
        "updatingActivitiesSequence" to "Ina-update ang mga aktibidad.....",
        "updatingLocationSequence" to "Ina-update ang lokasyon.....",
        "runningAutomaticActivitySequence" to "Isinasagawa ang check-in o check-out kapag naaangkop.....",
        "automaticUpdatesRunning" to "May kasalukuyang update.",
        "automaticUpdatesCompletedWithActivity" to "Tapos ang mga update at naisagawa ang {activity}.",
        "automaticUpdatesCompletedWithoutActivity" to "Tapos ang mga update nang walang naisagawang aktibidad.",
        "automaticUpdatesFailed" to "Hindi makumpleto ang mga awtomatikong update sa ngayon.",
        "automaticActivitiesDisabled" to "Aktibado na ang 100% manual mode.",
        "operationFailed" to "Hindi makumpleto ang operasyon.",
    ),
    "manual" to d(
        "eyebrow" to "Checking Web • Gabay sa paggamit",
        "heading" to "Mga Tagubilin sa Paggamit",
        "introPrimary" to "Binubuod ng manwal na ito ang mga pangunahing daloy ng pag-authenticate, pagrehistro, lokasyon, transportasyon, at suporta ng Checking Web.",
        "introSecondary" to "Gamitin ang pahinang ito bilang mabilis na sanggunian para maunawaan kung ano ang awtomatikong inaasikaso ng app at kung aling mga aksyon ang nananatili sa kontrol ng user.",
        "currentLanguageLabel" to "Wika ng pahina",
        "availabilityNote" to "Sa yugtong ito, ang kumpletong manwal ay makukuha sa Portuges at Ingles.",
        "highlights" to d(
            "accessTitle" to "Gabay na pag-access",
            "accessBody" to "Awtomatikong nagdedesisyon ang app kung kailan hihingi ng pagrehistro ng user, paggawa ng password, o karaniwang pag-authenticate.",
            "locationTitle" to "Sinusubaybayang lokasyon",
            "locationBody" to "Ang mga pahintulot, ang katumpakan ng GPS, at ang manwal na fallback ay direktang nakakaapekto sa pagrehistro ng attendance.",
            "supportTitle" to "Mabilis na tulong",
            "supportBody" to "Sa Settings nakasentro ang wika, password, lokasyon, suporta sa WhatsApp, at ang access sa dokumentasyong ito.",
        ),
        "tocTitle" to "Mapa ng manwal",
        "tocAriaLabel" to "Pag-navigate sa manwal",
        "snapshotSlotLabel" to "Slot ng snapshot",
        "toc" to d(
            "overview" to "Pangkalahatang-tanaw",
            "authFlow" to "Daloy ng pag-authenticate",
            "userRegistration" to "Awtomatikong pagrehistro ng user",
            "passwordRegistration" to "Awtomatikong pagrehistro ng password",
            "login" to "Pag-login",
            "attendance" to "Check-in at check-out",
            "projectSelection" to "Pagpili ng proyekto",
            "location" to "Pahintulot sa lokasyon",
            "automaticActivities" to "Awtomatikong mga aktibidad",
            "transport" to "Transportasyon",
            "passwordChange" to "Pag-reset o pagpalit ng password",
            "settings" to "Settings",
            "support" to "Suporta",
            "faq" to "Mga karaniwang problema at FAQ",
        ),
        "sections" to d(
            "overview" to d(
                "title" to "Pangkalahatang-tanaw",
                "lead" to "Pinagsasama ng Checking Web ang pag-authenticate, pagrehistro ng attendance, konteksto ng lokasyon, at access sa transportasyon sa iisang mobile-first na surface.",
                "item1" to "Ipinapakita ng pangunahing screen ang susi, ang password, ang kamakailang kasaysayan, ang form ng attendance, at ang shortcut papunta sa Transportasyon.",
                "item2" to "Awtomatikong lumalabas ang mga daloy ng tulong kapag wala pa ang susi o kapag kailangan pang gumawa ng unang password ang user.",
                "item3" to "Pinagsasama-sama ng menu ng Settings ang mga pangalawang aksyon para manatiling maluwag ang pangunahing bahagi.",
                "figureCaption" to "Pangunahing shell ng Checking Web kasama ang bahagi ng pag-authenticate at ang form ng attendance.",
            ),
            "authFlow" to d(
                "title" to "Daloy ng pag-authenticate",
                "lead" to "Sinusuri ng aplikasyon ang status ng susi sa sandaling umabot ito sa apat na wastong karakter at nagdedesisyon kung aling daloy ng tulong ang ipapakita.",
                "item1" to "Kung wala ang susi, lumilipat ang daloy sa pagrehistro ng user nang hindi naghihintay ng karagdagang pag-tap.",
                "item2" to "Kung umiiral ang susi ngunit wala pang password, direktang binubuksan ng interface ang pagrehistro ng password.",
                "item3" to "Kung umiiral na ang susi at password, nananatili ang interface sa karaniwang daan ng pag-login.",
                "note" to "Ang manwal na pagsasara ng isang modal ay hindi lumilikha ng walang katapusang loop: muli lamang itong susubok kapag talagang nagbago ang susi o ang nauugnay na auth state.",
            ),
            "userRegistration" to d(
                "title" to "Awtomatikong pagrehistro ng user",
                "lead" to "Kapag wala ang susi sa database, binubuksan ng app ang self-service na form para makumpleto ng bagong user ang pagrehistro.",
                "item1" to "Hinihingi ng form ang susi, buong pangalan, mga proyekto, opsyonal na email, password, at kumpirmasyon ng password.",
                "item2" to "Nilo-load ang mga proyekto mula sa API at maaaring markahan ng user ang isa o higit pang wastong item.",
                "item3" to "Pagkatapos ng matagumpay na pagsumite, na-authenticate na ang web session at nananatiling bukas ang app para magamit.",
                "figureCaption" to "Modal ng pagrehistro ng bagong user na awtomatikong binuksan para sa isang hindi kilalang susi.",
            ),
            "passwordRegistration" to d(
                "title" to "Awtomatikong pagrehistro ng password",
                "lead" to "Kung umiiral na ang susi ngunit wala pang nakarehistrong password, pumapasok ang Checking Web sa mode ng paggawa ng unang password.",
                "item1" to "Muling ginagamit ng modal ang umiiral na bahagi ng password, ngunit itinatago nito ang field ng lumang password.",
                "item2" to "Inilalagay ng user ang bagong password, kinukumpirma ang halaga, at kinukumpleto ang unang access.",
                "item3" to "Pagkatapos i-save, ina-authenticate ng backend ang web session at tinatanggap na ng pangunahing screen ang iba pang operasyon.",
                "figureCaption" to "Modal ng paggawa ng unang password para sa isang user na umiiral na sa sistema.",
            ),
            "login" to d(
                "title" to "Pag-login",
                "lead" to "Ang mga user na umiiral na ay nag-ti-type ng susi, naglalagay ng password, at hinahayaan ang app na i-validate ang pag-authenticate bago magrehistro ng attendance o magbukas ng Transportasyon.",
                "item1" to "Maaari pa ring awtomatikong ma-validate ang password sa pamamagitan ng umiiral na daloy ng pangunahing shell.",
                "item2" to "Kung babaguhin ng user ang na-type na password pagkatapos mag-authenticate, muling nirerepe ng app ang sarili hanggang ma-verify ang bagong halaga.",
                "item3" to "Tumutulong ang mga mensahe ng status na ikaiba ang naghihintay ng password, nagve-verify, at na-authenticate.",
            ),
            "attendance" to d(
                "title" to "Check-in at check-out",
                "lead" to "Sa wastong pag-authenticate, pinipili ng user ang uri ng pagrehistro, sinusuri ang konteksto, at isinusumite ang operasyon mula sa pangunahing screen.",
                "item1" to "Ikinaiiba ng form ang check-in sa check-out at maaaring harangan ang manwal na pagsumite kapag kinokontrol ng awtomatikong mga aktibidad ang daloy.",
                "item2" to "Ang nakikitang kasaysayan malapit sa itaas ay tumutulong kumpirmahin ang huling naitalang event bago magpadala ng bago.",
                "item3" to "Lumalabas ang mga mensahe ng tagumpay o kabiguan sa bahagi ng status pagkatapos ng bawat pagtatangka.",
                "figureCaption" to "Halimbawa ng matagumpay na estado ng pagrehistro ng attendance.",
            ),
            "projectSelection" to d(
                "title" to "Pagpili ng proyekto",
                "lead" to "Ginagamit ng app ang mga proyekto kapwa sa unang pagrehistro at sa pang-araw-araw na daloy, para limitahan ang konteksto ng user at ng mga available na lokasyon.",
                "item1" to "Sa pagrehistro, minamarkahan ng user ang isa o higit pang wastong proyekto bago kumpletuhin ang paggawa ng account.",
                "item2" to "Pagkatapos ma-authenticate, ipinapakita ng pangunahing panel ang kasalukuyang mga proyekto at pinapayagan ang update kapag aktibo ang kakayahang iyon.",
                "item3" to "Ang aktibong proyekto ay nakakaapekto sa mga listahan ng lokasyon, kontekstuwal na kasaysayan, at iba pang bahaging nakadepende sa saklaw ng user.",
                "figureCaption" to "Halimbawa ng bloke ng mga proyekto sa na-authenticate na shell, na nagpapakita ng buod ng aktibong saklaw.",
            ),
            "location" to d(
                "title" to "Pahintulot sa lokasyon at pag-uugali ng GPS",
                "lead" to "Ginagamit ang posisyon ng device para matukoy ang konteksto ng operasyon, paganahin ang mga awtomatikong daloy, at gabayan ang user kapag hindi pa sapat ang katumpakan.",
                "item1" to "Nakadepende ang aplikasyon sa HTTPS, suporta ng browser, at aktibong pahintulot para humiling ng tumpak na lokasyon.",
                "item2" to "Kapag tinanggihan o hindi available ang pahintulot, nagpapakita ang app ng malinaw na mga mensahe at maaaring limitahan ang daloy sa pinahihintulutang manwal na fallback.",
                "item3" to "Kapag matagumpay na nakuha ang lokasyon, ina-update ng interface ang katumpakan, ang nakilalang lugar, at ang mga kaugnay na awtomatikong aksyon.",
                "figureCaptionDenied" to "Halimbawa ng screen kapag hindi pa available ang pahintulot sa lokasyon.",
                "figureCaptionGranted" to "Halimbawa ng screen pagkatapos matagumpay na maipagkaloob ang access sa tumpak na lokasyon.",
            ),
            "automaticActivities" to d(
                "title" to "Awtomatikong mga aktibidad",
                "lead" to "Kaya rin ng app na mag-trigger ng check-in, check-out, at mga kaugnay na update kapag nagbago ang konteksto ng lokasyon sa inaasahang paraan.",
                "item1" to "Nakadepende ang mekanismong ito sa wastong pag-authenticate, mga pagbasa ng lokasyon, at mga panloob na panuntunan na umiiwas sa hindi ligtas na transisyon.",
                "item2" to "Kapag kinokontrol ng awtomatikong mga aktibidad ang daloy, may ilang manwal na field na nagiging nakatago o naka-disable para protektahan ang pagkakapare-pareho ng record.",
                "item3" to "Kung bumaba ang katumpakan sa ibaba ng kinakailangan, maaaring muling buksan ng app ang mga opsyon ng manwal na fallback sa halip na gumawa ng mapanganib na pagpapalagay.",
            ),
            "transport" to d(
                "title" to "Access sa Transportasyon",
                "lead" to "Pagkatapos ng pag-authenticate, maaaring buksan ng user ang module ng Transportasyon para humiling ng biyahe, tingnan ang status, at suriin ang mga detalye ng pinakahuling kahilingan.",
                "item1" to "Nananatiling protektado ang access ng parehong susi at password na na-validate sa pangunahing shell.",
                "item2" to "Kasama sa module ang pagrehistro ng address, kahilingan ayon sa uri ng transportasyon, at isang buod ng kasalukuyang status.",
                "item3" to "Kapag may na-allocate na sasakyan, ipinapakita ng screen ang mga pangunahing detalye ng operasyon para sa user.",
                "figureCaption" to "Halimbawa ng surface ng Transportasyon na binuksan mula sa shell ng Checking Web.",
            ),
            "passwordChange" to d(
                "title" to "Pag-reset o pagpalit ng password",
                "lead" to "Hindi na nasa pangunahing hanay ng pag-authenticate ang pagpalit ng password at nakasentro na ito ngayon sa Settings.",
                "item1" to "Buksan ang Settings, i-tap ang Palitan ang Password, at gamitin ang umiiral na modal ng pagpalit ng password.",
                "item2" to "Hinihingi ng daloy ang lumang password, bagong password, at kumpirmasyon, habang pinapanatili ang umiiral na mga validation.",
                "item3" to "Aktibo lamang ang aksyon kapag na-authenticate na ang user at mayroon nang nakarehistrong password.",
                "figureCaption" to "Daloy ng pagpalit ng password na sinimulan mula sa Settings > Palitan ang Password.",
            ),
            "settings" to d(
                "title" to "Settings",
                "lead" to "Binubuksan ng gear icon ang isang sentral na lugar para sa mga kagustuhan at pangalawang aksyon nang hindi pinupuno ang hanay ng pag-authenticate.",
                "item1" to "Ina-update ng Wika ang mga nakikitang label ng pangunahing aplikasyon at pinapanatili ang kagustuhan sa storage ng browser.",
                "item2" to "Muling ginagamit ng Payagan ang Lokasyon ang umiiral na pipeline para muling humiling ng tumpak na access kapag kailangan pa ito.",
                "item3" to "Ginagamit ng Suporta at Tungkol Dito ang parehong entry point para buksan ang WhatsApp at ang dokumentasyong ito.",
                "figureCaption" to "Widget ng Settings na may bagong sentralisadong pangalawang mga aksyon.",
            ),
            "support" to d(
                "title" to "Suporta",
                "lead" to "Kapag kailangan ng user ng tulong ng tao, inihahanda ng Settings > Suporta ang isang usapan sa WhatsApp na kasama na ang susi ng user sa unang mensahe.",
                "item1" to "Ginagamit ng link ang opisyal na numero ng telepono na naka-configure sa frontend ng Checking Web.",
                "item2" to "Awtomatikong inihahanda ang panimulang mensahe para mabawasan ang abala at mapabilis ang suporta.",
                "item3" to "Kung walang available na wastong susi, nananatiling naka-disable ang button ng suporta para sa kaligtasan.",
            ),
            "faq" to d(
                "title" to "Mga karaniwang problema at FAQ",
                "lead" to "Gamitin ang mabilis na mga sagot na ito kapag tila natigil ang interface o kapag hindi sumusulong ang daloy gaya ng inaasahan.",
                "q1" to "Bakit nagbukas ang app ng daloy ng pagrehistro nang mag-isa?",
                "a1" to "Nangyayari ito kapag wala ang susi o kapag wala pang password ang account. Inaalis ng bagong daloy ang dagdag na pag-tap at dinadala kaagad ang user sa tamang aksyon.",
                "q2" to "Bakit naka-disable ang button na Payagan ang Lokasyon?",
                "a2" to "Dahil naiintindihan na ng aplikasyon na aktibo na ang tumpak na pahintulot, o dahil may kasalukuyang isinasagawang refresh ng lokasyon.",
                "q3" to "Ano ang dapat kong gawin kung hindi nagbubukas ang Transportasyon?",
                "a3" to "Tiyakin muna na na-authenticate ang susi at password. Kung magpatuloy ang problema, buksan ang Suporta para maipadala ang iyong susi sa team ng suporta.",
            ),
            "scheduledPause" to d(
                "title" to "Naka-iskedyul na Pause",
                "lead" to "Nakakatipid ng baterya ang Naka-iskedyul na Pause sa pamamagitan ng pag-suspende sa awtomatikong mga aktibidad sa loob ng isang yugto (halimbawa, sa gabi).",
                "item1" to "Sa Settings, i-tap ang Naka-iskedyul na Pause.",
                "item2" to "I-on ang opsyon at itakda ang oras na Mula at Hanggang (halimbawa, 22:00 hanggang 06:00).",
                "item3" to "Kung gusto mo, lagyan din ng check ang I-pause tuwing Sabado at/o I-pause tuwing Linggo. Sa panahon ng pause, hindi gumagawa ang app ng anumang awtomatikong aktibidad at nagpapatuloy ito nang mag-isa sa katapusan ng yugto.",
            ),
            "accident" to d(
                "title" to "Sakaling magkaroon ng aksidente",
                "lead" to "Ang Accident Mode ay isang feature para sa kaligtasan. Gamitin lamang ito sa tunay na emergency.",
                "item1" to "Sinumang user ay maaaring magbukas ng Accident Mode; inaabisuhan nito, sa real time, ang lahat ng user sa parehong proyekto.",
                "item2" to "Iulat ang iyong sitwasyon at zone: ligtas, nasa lugar ng aksidente ngunit ligtas, o nasa lugar ng aksidente at nangangailangan ng tulong. Kung maaari, mag-record ng video ng lugar — ipinapadala ito sa real time sa dashboard ng administrator.",
                "item3" to "Ang button na Tawagan ang Serbisyong Pang-emergency ay tumatawag sa lokal na serbisyong pang-emergency, iniuulat ang aksidente at lokasyon sa wika ng rehiyon.",
            ),
        ),
        "figures" to d(
            "authShellAlt" to "Pangunahing shell ng Checking Web na may mga field ng susi at password.",
            "userRegistrationAlt" to "Form ng pagrehistro ng bagong user sa Checking Web.",
            "passwordRegistrationAlt" to "Modal ng unang pagrehistro ng password para sa umiiral na user na walang password.",
            "settingsModalAlt" to "Widget ng Settings ng Checking Web na bukas na may mga aksyon para sa wika, password, lokasyon, suporta, at tungkol dito.",
            "passwordChangeAlt" to "Modal ng pagpalit ng password na binuksan mula sa Settings.",
            "locationDeniedAlt" to "Estado ng Checking Web na tinanggihan o hindi available ang pahintulot sa lokasyon.",
            "locationGrantedAlt" to "Estado ng Checking Web na may available at naibabahaging tumpak na lokasyon.",
            "projectSelectionAlt" to "Bahagi ng pagpili ng proyekto sa Checking Web.",
            "transportScreenAlt" to "Screen ng module ng Transportasyon sa loob ng ecosystem ng Checking Web.",
            "checkSuccessAlt" to "Matagumpay na estado ng check-in o check-out sa Checking Web.",
        ),
    ),
    "accident" to d(
        "button" to d(
            "report" to "Iulat ang Aksidente",
            "reported" to "Naiulat na ang Aksidente",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "Kailangan ko ng tulong sa Web application. Ang susi ko ay {chave}.",
    ),
    "instructions" to d(
        "heading" to "Mga Tagubilin",
        "intro" to "Ipinapakita ng gabay na ito, hakbang-hakbang, kung paano gamitin ang Checking: mag-record ng attendance nang manu-mano, i-on ang Awtomatikong Mode (check-in at check-out ayon sa lokasyon) at i-set up ang Naka-iskedyul na Pause.",
        "step1" to d(
            "title" to "1. Mag-log in sa app",
            "item1" to "Sa home screen, ilagay ang iyong 4-character na susi sa field na 'Susi'. Nagiging kahel ang field kapag natagpuan ang susi.",
            "item2" to "Ilagay ang iyong password sa field na 'Password'. Kapag na-verify, nagiging berde ang mga field at lalabas ang 'Tapos na ang pag-verify'.",
            "item3" to "Kung wala ka pang password, awtomatikong bubuksan ng app ang paggawa ng password; kung wala ang susi, mag-aalok ito ng self-registration.",
        ),
        "step2" to d(
            "title" to "2. Mag-record ng attendance nang manu-mano",
            "item1" to "Piliin ang 'Check-In' o 'Check-Out' at ang uri na 'Normal' o 'Retroactive'.",
            "item2" to "Kapag naka-off ang Awtomatikong Mode, piliin ang 'Lokasyon' sa listahan at i-tap ang 'I-record ang Check-In' (o 'Check-Out').",
            "item3" to "Ipinapakita ng card sa itaas ang iyong huling check-in at check-out; i-tap ito para makita ang buong listahan na may petsa, oras, at lokasyon.",
        ),
        "step3" to d(
            "title" to "3. I-on ang Awtomatikong Mode",
            "lead" to "Sa Awtomatikong Mode, gumagawa ang app ng check-in at check-out nang mag-isa batay sa iyong lokasyon — kapag pumapasok o umaalis sa isang nakarehistrong lugar, kapag dinala ang app sa foreground, at sa pana-panahong pagsusuri.",
            "item1" to "I-tap ang gear icon (katabi ng mga field ng susi/password) para buksan ang 'Settings'.",
            "item2" to "I-tap ang 'Awtomatikong mga Aktibidad' at lagyan ng check ang kahon na 'I-enable ang Awtomatikong mga Aktibidad'.",
            "item3" to "Ipagkaloob ang bawat pahintulot sa listahang lalabas sa pamamagitan ng pag-tap dito: Mga Abiso, Lokasyon na 'Palaging payagan', walang limitasyong paggamit ng Baterya, at — sa ilang device — 'Magsimula kasama ng device'.",
            "item4" to "Kapag BERDE ang ningning ng gear, aktibo at malusog ang Awtomatikong Mode. Ang KAHEL na ningning ay nagpapahiwatig na may kulang na inirerekomendang pahintulot.",
            "callout" to "Mahalaga: para gumana nang maaasahan sa background, ibigay ang Lokasyon bilang 'Palaging payagan' at i-off ang battery optimization para sa Checking.",
        ),
        "step4" to d(
            "title" to "4. I-on ang Naka-iskedyul na Pause",
            "lead" to "Nakakatipid ng baterya ang Naka-iskedyul na Pause sa pamamagitan ng pag-pause sa awtomatikong mga aktibidad sa loob ng isang yugto (halimbawa, sa gabi).",
            "item1" to "Sa 'Settings', i-tap ang 'Naka-iskedyul na Pause'.",
            "item2" to "I-on ang opsyon at itakda ang oras na 'Mula' at 'Hanggang' (halimbawa, 22:00 hanggang 06:00).",
            "item3" to "Kung gusto mo, lagyan din ng check ang 'I-pause tuwing Sabado' at/o 'I-pause tuwing Linggo'.",
            "item4" to "Sa panahon ng pause, hindi gumagawa ang app ng anumang awtomatikong aktibidad; nagpapatuloy ito nang mag-isa sa katapusan ng yugto.",
        ),
        "step5" to d(
            "title" to "5. Subaybayan ang kasaysayan",
            "item1" to "I-tap ang 'HULING CHECK-IN' o 'HULING CHECK-OUT' para buksan ang talahanayan na may Petsa, Oras, at Lokasyon ng bawat record.",
            "item2" to "Ang mga record na ginawa malapit ngunit sa labas ng isang nakarehistrong lugar ay lumalabas bilang 'Hindi Nakarehistrong Lokasyon'.",
            "item3" to "Kahit walang internet, naka-save sa device ang iyong mga record at ipinapadala kaagad pagbalik ng koneksyon, palaging may orihinal na oras.",
        ),
        "step6" to d(
            "title" to "6. Humiling ng transportasyon",
            "item1" to "I-tap ang 'Transportasyon' para buksan ang module ng transportasyon ng tauhan.",
            "item2" to "Ilagay ang address at oras na kailangan at isumite ang kahilingan.",
            "item3" to "Inaayos ng namamahala sa logistics ang mga biyahe; nagmumungkahi ang isang artificial-intelligence engine kung paano pagpangkatin ang mga pasahero at ayusin ang mga hinto.",
        ),
        "step7" to d(
            "title" to "7. Sakaling magkaroon ng aksidente",
            "lead" to "Ang Accident Mode ay isang feature para sa kaligtasan. Gamitin lamang ito sa tunay na emergency.",
            "item1" to "Sinumang user ay maaaring magbukas ng Accident Mode; inaabisuhan nito, sa real time, ang lahat ng user sa parehong proyekto.",
            "item2" to "Iulat ang iyong sitwasyon at zone: 'ligtas', 'nasa lugar ng aksidente ngunit ligtas', o 'nasa lugar ng aksidente at nangangailangan ng tulong'.",
            "item3" to "Kung maaari, mag-record ng video ng lugar: ipinapadala ito sa real time sa dashboard ng administrator.",
            "item4" to "Ang button na 'Tawagan ang Serbisyong Pang-emergency' ay tumatawag sa lokal na serbisyong pang-emergency, iniuulat ang aksidente at lokasyon sa wika ng rehiyon.",
        ),
        "step8" to d(
            "title" to "8. Iba pang settings",
            "item1" to "'Mga Abiso': piliin kung aling mga notification ang matatanggap mo (mga aktibidad, naka-iskedyul na pause, aksidente).",
            "item2" to "'Wika': palitan ang wika ng app.",
            "item3" to "'Palitan ang Password': magtakda ng bagong password kapag kailangan.",
            "item4" to "'Suporta': makipag-usap nang direkta sa team sa WhatsApp.",
            "item5" to "'Tungkol Dito': alamin ang kasaysayan ng Checking at ang mga bahaging bumubuo sa sistema.",
        ),
        "closing" to "Ayan! Kapag naka-on ang Awtomatikong Mode, hindi mo na kailangang mag-record ng anuman nang manu-mano — inaasikaso ito ng Checking para sa iyo.",
    ),
    "about" to d(
        "heading" to "Tungkol sa Checking",
        "introTitle" to "Kung paano nagsimula ang Checking",
        "introBody" to "Nagsimulang buuin ang Checking noong Marso 2026, mula sa ideya ni Engineer Dilnei Schmidt.\n" +
            "\n" +
            "May pangangailangang mabilis na matukoy ang bawat empleyado ng Petrobras na naroon sa lugar ng trabaho ng konstruksyon at pagkakabit, sakaling magkaroon ng aksidente.\n" +
            "\n" +
            "Ang unang solusyon ng pamunuan ng SMS ay isang online na form, na pinupunan ng bawat empleyado pagdating at pag-alis sa lugar ng trabaho. Gumana ito sa pagtukoy kung sino ang naroon, ngunit nakakapagod ito at marami ang minsang nakakalimot punan.\n" +
            "\n" +
            "Para mapabuti ang kahusayan, gumawa si Dilnei ng app na kayang:\n" +
            "• tukuyin, sa pamamagitan ng GPS, ang lapit ng user sa lugar ng trabaho at paalalahanan siyang mag-check-in;\n" +
            "• magtakda ng mga alarm sa karaniwang oras ng check-in at check-out, na nagpapaalala sa user na punan ang form;\n" +
            "• awtomatikong punan ang form gamit ang datos ng user at isumite ito online.\n" +
            "\n" +
            "Pinadali nito ang trabaho at pinataas ang dalas ng pagpuno ng form.\n" +
            "\n" +
            "Noon ding Marso 2026, nalaman ni Engineer Tamer Salmem ang mga naipatupad na solusyon at isinulong ang paggamit ng kasalukuyang teknolohiya sa programming, na binubuo ang sistemang unang inisip ni Dilnei.\n" +
            "\n" +
            "Layunin nito na hindi na kailangang mag-alala ang user na magbukas ng app para mag-check-in o check-out. Bukod dito, gumawa ng real-time na pagsubaybay para malaman ng mga administrator hindi lamang kung sino ang nasa trabaho, kundi kung sa aling nakarehistrong lokasyon ng bawat proyekto naroon ang bawat user — na nagpapataas ng kakayahang tumugon sa mga emergency.\n" +
            "\n" +
            "Kaya, nadagdagan ang sistema ng:\n" +
            "• pag-activate ng mga serbisyo sa pamamagitan ng geofencing (batay sa lapit ng user sa lugar ng trabaho);\n" +
            "• pagsasagawa ng mga gawain sa background — check-in sa bawat pagbabago ng lokasyon sa loob ng pasilidad at check-out kapag lumalayo ang user, nang hindi man lang ina-unlock ang device;\n" +
            "• real-time na pagpapadala ng lokasyon ng mga user sa dashboard ng administrator;\n" +
            "• kakayahang magrehistro ng kahit ilang proyekto na kailangan, kahit saan sa mundo.\n" +
            "\n" +
            "Maaari ring pumasok ang sistema sa 'Accident Mode'. Sakaling magkaroon ng aksidente, sinumang user ay maaaring mag-trigger ng alarm na nag-aabiso, sa real time, sa lahat ng user sa parehong proyekto. Kapag aktibo ang Accident Mode:\n" +
            "• may talahanayang nalilikha sa dashboard ng administrator, na naglilista ng sitwasyon ng bawat user: 'ligtas', 'nasa lugar ng aksidente ngunit ligtas', at 'nasa lugar ng aksidente at nangangailangan ng tulong';\n" +
            "• maaaring mag-record ang user ng video at ipadala ito sa real time, bilang link sa talahanayan, para makita ng administrator ang mga eksena ng lugar;\n" +
            "• ang button na 'Tawagan ang Serbisyong Pang-emergency' ay tumatawag sa nakarehistrong lokal na serbisyong pang-emergency, iniuulat ang aksidente, ang lokasyon, at ang taong makokontak, na nagsasalita sa wika ng rehiyon.\n" +
            "\n" +
            "Ang katatagan at pagiging maaasahan ng sistema ay nagdala ng operational safety at agarang pagtugon sa team ng SMS ng Petrobras.\n" +
            "\n" +
            "Sa wakas, isinama ni Engineer Thiago Soares do Nascimento ang impormasyong nililikha ng sistema sa mga kasalukuyang management dashboard, upang gumana ang bagong sistema kasabay ng lumang pagpuno ng form, na pinananatiling napapanahon ang mga kontrol sa pamamahala.\n" +
            "\n" +
            "Ganito isinilang ang CHECKING.",
        "partsTitle" to "Ang mga bahagi ng sistema",
        "partsIntro" to "Ang Checking ay isang sistema ng kontrol sa attendance na nagre-record ng pagpasok at paglabas ng mga empleyado sa mga lugar ng trabaho. Gumagana ito sa iba't ibang channel — mga RFID card reader na naka-install sa lugar, isang Android app, isang web page na naa-access mula sa telepono, at isang administration panel — na pinagsasama ang lahat sa isang lugar.\n" +
            "\n" +
            "Binubuo ito ng:\n" +
            "• isang API, na ginawa sa Python/FastAPI;\n" +
            "• isang website para sa mga administrator ng sistema;\n" +
            "• isang Web application, responsive para sa mga telepono at desktop;\n" +
            "• isang dashboard para sa transportasyon ng tauhan;\n" +
            "• isang Android-only na app, na ginawa sa Kotlin.",
        "partApiTitle" to "API",
        "partApiBody" to "Ang API ang utak ng sistema. Sa tuwing may mag-check-in o check-out — sa pamamagitan ng pisikal na reader, ng app, o ng web page — ito ang tumatanggap ng impormasyon, nagve-verify kung tama, nagse-save sa database, at nag-aabiso sa iba pang component sa real time.\n" +
            "\n" +
            "Awtomatiko rin nitong pinupunan ang corporate na Microsoft Forms pagkatapos ng bawat record, kinokoordina ang sistema ng transportasyon, nag-tri-trigger ng mga emergency alert sakaling magkaroon ng aksidente, at tinitiyak na walang datos na mawawala kapag hindi matatag ang koneksyon.",
        "partWebsiteTitle" to "Website",
        "partWebsiteBody" to "Ang website ang control panel ng mga administrator. Sa pamamagitan nito, makikita sa real time kung sino ang naka-check-in at sino ang naka-check-out, at mapamamahalaan ang bawat aspeto ng sistema nang walang teknikal na kaalaman.\n" +
            "\n" +
            "Mga pangunahing tungkulin: magrehistro at mag-edit ng mga empleyado, gumawa ng mga proyekto at ang kanilang mga panuntunan, tukuyin ang mga heograpikong lugar na kinikilala ng sistema, tingnan ang mga ulat ng attendance, at mag-export ng datos. Ito rin ang sentral na punto para i-trigger at subaybayan ang Accident Mode — nakikita ang sitwasyon ng bawat empleyado sa real time at kinokoordina ang emergency response.",
        "partWebappTitle" to "Web application",
        "partWebappBody" to "Ang web application ang kasangkapan ng mga empleyado. Gumagana ito sa browser ng telepono o computer, walang kailangang i-install, at nagbibigay-daan sa bawat tao na mag-record ng pagpasok o paglabas, tingnan ang kanilang kasaysayan, at humiling ng transportasyon.\n" +
            "\n" +
            "Kapag in-on ng empleyado ang Awtomatikong mga Aktibidad, ang telepono mismo ang tumutukoy sa lokasyon at nagche-check-in o check-out nang awtomatiko kapag pumapasok o umaalis sa mga nakarehistrong lugar. Sakaling magkaroon ng aksidente, nagbabago ang interface at hinihiling sa empleyado na iulat ang kanyang sitwasyon at safety zone.\n" +
            "\n" +
            "Available ito sa anim na wika (Portuges, Ingles, Tsino, Malay, Indonesian, at Tagalog) para sa mga internasyonal na team.",
        "partTransportTitle" to "Dashboard ng transportasyon",
        "partTransportBody" to "Ang dashboard ng transportasyon ang kasangkapan ng namamahala sa logistics ng biyahe. Sa pamamagitan nito, maaaring magrehistro ng mga sasakyan, tingnan at ayusin ang mga kahilingan sa transportasyon ng mga empleyado, at italaga ang bawat tao sa isang sasakyan para sa araw na iyon.\n" +
            "\n" +
            "May kasama itong artificial-intelligence engine na sinusuri ang mga address at oras at awtomatikong nagmumungkahi kung paano pagpangkatin ang mga pasahero at ayusin ang mga hinto sa pinakamainam na paraan — na binabawasan ang oras ng biyahe at bilang ng mga biyahe. Maaaring tanggapin ng namamahala ang mungkahi, i-adjust ito, o gawin ang pagtatalaga nang manu-mano.",
        "partAndroidTitle" to "Android app",
        "partAndroidBody" to "Ang Android app ay nag-aalok ng parehong tungkulin tulad ng web application, na may mas kumpletong karanasan sa araw-araw. Ang pangunahing bentahe nito ay ang automation sa pamamagitan ng geolocation: tumatakbo ang app sa background at nagre-record ng check-in o check-out nang awtomatiko habang pumapasok at umaalis ang empleyado sa mga nakarehistrong lugar, nang hindi umaasa sa browser.\n" +
            "\n" +
            "Gumagana rin ito nang walang internet: kapag walang koneksyon, naka-save sa telepono ang mga record at ipinapadala kaagad pagbalik ng koneksyon, palaging may orihinal na oras. Kasama rin dito ang kasaysayan na may petsa, oras, at lokasyon ng bawat event, ang module ng transportasyon, at ang emergency mode para sa mga aksidente.",
        "rulesTitle" to "Mga sitwasyon ng check-in at check-out",
        "rulesIntro" to "Inilalarawan ng mga sitwasyon sa ibaba, hakbang-hakbang, kung ano ang dapat gawin ng sistema para sa bawat user (check-in o check-out) sa bawat karaniwang senaryo. Sinusunod ng Web Application at ng Native App ang mga panuntunan ng kani-kanilang bloke.",
        "rulesWebTitle" to "Mga sitwasyon — Web Application",
        "rulesWebBody" to "## Sitwasyon 1 — Check-out sa paglayo\n" +
            "• Naka-on ang Awtomatikong mga Aktibidad, na may buong pahintulot sa lokasyon.\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ina-update ng app ang lokasyon at napag-aalaman na ang user ay nasa 'Check-Out Zone' o mahigit 2 km mula sa anumang nakarehistrong lugar (maliban sa Check-Out Zone).\n" +
            "• Dahil ang huling aktibidad ay check-in, gumagawa ang app ng check-out.\n" +
            "\n" +
            "## Sitwasyon 2 — Naka-check-out na, malayo o nasa Check-Out Zone\n" +
            "• Ang huling aktibidad ay check-out.\n" +
            "• Ang user ay nasa 'Check-Out Zone' o mahigit 2 km mula sa anumang nakarehistrong lugar.\n" +
            "• Walang aksyon: hindi inuulit ang check-out dahil sa pagbabago ng lokasyon.\n" +
            "\n" +
            "## Sitwasyon 3 — Pagdating sa trabaho (check-in)\n" +
            "• Ang huling aktibidad ay check-out.\n" +
            "• Ang user ay nasa LOOB ng isang nakarehistrong lugar maliban sa 'Check-Out Zone' (tunay na pagtutugma sa lugar, hindi lamang lapit).\n" +
            "• Talagang nasa trabaho ang user (kasama ang unang check-in ng araw).\n" +
            "• Gumagawa ang app ng check-in at ina-update ang lokasyon sa katugmang nakarehistrong lugar.\n" +
            "! MAHALAGA: kung ang user ay WALA sa loob ng anumang nakarehistrong lugar — kahit malapit (wala pang 2 km mula sa isang coordinate, maliban sa Check-Out Zone) — HINDI awtomatikong nagche-check-in ang app; ipinapakita lang nito ang 'Hindi Nakarehistrong Lokasyon' (katulad ng Sitwasyon 5).\n" +
            "\n" +
            "## Sitwasyon 4 — Bagong check-in (palagi)\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ang user ay nasa isang nakarehistrong lugar maliban sa 'Check-Out Zone'.\n" +
            "• Gumagawa ang app ng bagong check-in ANUMAN ang nangyari sa lokasyon, nagbago man o hindi.\n" +
            "• Kahit nasa PAREHONG lugar ng huling check-in, ginagawa ang bagong check-in para i-record/i-update ang lokasyon at oras.\n" +
            "\n" +
            "## Sitwasyon 5 — Malapit ngunit nasa labas ng lugar\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ang user ay wala sa anumang nakarehistrong lugar, ngunit hindi rin mahigit 2 km mula sa isang nakarehistrong coordinate (maliban sa Check-Out Zone). Ibig sabihin, malapit ang user sa trabaho.\n" +
            "• Walang aksyon: ipinapakita lang ng app ang 'Hindi Nakarehistrong Lokasyon'.\n" +
            "\n" +
            "## Sitwasyon 6 — Button na 'I-refresh' pagkatapos ng check-in\n" +
            "• Nasa foreground na ang app; ang huling aktibidad ay check-in.\n" +
            "• I-tap ng user ang 'I-refresh' para i-update ang lokasyon.\n" +
            "• Gumagawa ang app ng bagong check-in ANUMAN ang nangyari sa lokasyon, para i-record/i-update ang lokasyon at oras.\n" +
            "\n" +
            "## Sitwasyon 7 — Pag-alis sa Check-Out Zone\n" +
            "• Nasa foreground; ang huling aktibidad ay check-out; ang user ay nasa 'Check-Out Zone' (walang aksyon).\n" +
            "• I-tap ng user ang 'I-refresh' at napag-aalaman ng app na umalis siya sa Check-Out Zone, patungo sa:\n" +
            "• Variant 7A — isang nakarehistrong lugar maliban sa 'Check-Out Zone';\n" +
            "• Variant 7B — walang nakarehistrong lugar, ngunit malapit pa rin (wala pang 2 km, maliban sa Check-Out Zone).\n" +
            "• Sa dalawa, agad na nagche-check-in ang app, ina-update ang lokasyon sa nakarehistrong lugar o, kung walang eksaktong tugma, sa 'Hindi Nakarehistrong Lokasyon'.\n" +
            "\n" +
            "## Sitwasyon 8 — Mixed Zone\n" +
            "• Natutukoy ng app na tumutugma ang posisyon sa 'Mixed Zone' (sa unang pagpasok o sa magkasunod na pagbasa).\n" +
            "• Kung ang huling kaugnay na aktibidad ay HINDI sa mismong 'Mixed Zone', agaran ang paglipat: 8A — pagkatapos ng check-in → check-out sa 'Mixed Zone'; 8B — pagkatapos ng check-out → check-in sa 'Mixed Zone'.\n" +
            "• Ang field na 'Time Interval ng Mixed Zone' (tab na 'Pagrehistro' ng admin website) ang cooldown para sa magkasunod na pagbasa sa Mixed Zone lamang: habang nakalipas_na_oras < interval, naka-block ang bagong paglipat; kapag >= interval, pinapayagan itong muli.\n" +
            "• Eksepsyon pagkatapos ng check-in sa Mixed Zone: pagpunta sa 'Check-Out Zone' o lampas sa 'Pinakamababang distansya para sa awtomatikong check-out' → agarang check-out, nang hindi naghihintay ng cooldown.\n" +
            "• Eksepsyon pagkatapos ng check-out sa Mixed Zone: pagpunta sa ibang nakarehistrong lugar (maliban sa Check-Out Zone at Mixed Zone) o pananatili sa loob ng pinakamababang distansya → agarang check-in, isinasawalang-bahala ang cooldown.\n" +
            "\n" +
            "## Sitwasyon 9 — Manu-manong mode (naka-off ang Awtomatikong mga Aktibidad)\n" +
            "• Naka-authenticate ang user; NAKA-OFF ang Awtomatikong mga Aktibidad.\n" +
            "• Ina-update ng app ang lokasyon kung may pahintulot; kung wala, ipinapakita nito ang 'Tinanggihan ang pahintulot'.\n" +
            "• Pinipili ng user ang 'check-in' o 'check-out', 'Normal' o 'Retroactive', pinipili ang 'Lokasyon' (available tuwing naka-off ang Awtomatikong mga Aktibidad), at i-tap ang 'I-record'.\n" +
            "• Sinusunod ng app ang normal na daloy at isinasagawa ang aktibidad ayon sa mga pinili.",
        "rulesNativeTitle" to "Mga sitwasyon — Native App (Android)",
        "rulesNativeBody" to "## Sitwasyon 1 — Check-out sa paglayo\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ang user ay nasa 'Check-Out Zone' o mahigit 2 km mula sa anumang nakarehistrong lugar (maliban sa Check-Out Zone).\n" +
            "• Gumagawa ang app ng check-out.\n" +
            "\n" +
            "## Sitwasyon 2 — Naka-check-out na, malayo o nasa Check-Out Zone\n" +
            "• Ang huling aktibidad ay check-out.\n" +
            "• Ang user ay nasa 'Check-Out Zone' o mahigit 2 km mula sa anumang nakarehistrong lugar.\n" +
            "• Walang aksyon: hindi inuulit ang check-out dahil sa pagbabago ng lokasyon.\n" +
            "\n" +
            "## Sitwasyon 3 — Pagdating sa trabaho (check-in)\n" +
            "• Ang huling aktibidad ay check-out.\n" +
            "• Ang user ay nasa LOOB ng isang nakarehistrong lugar maliban sa 'Check-Out Zone' (tunay na pagtutugma, hindi lamang lapit).\n" +
            "• Gumagawa ang app ng check-in at ina-update ang lokasyon sa katugmang lugar.\n" +
            "! MAHALAGA: mula sa CHECK-OUT, kung ang user ay WALA sa loob ng anumang nakarehistrong lugar — kahit malapit — HINDI nagche-check-in ang app; ipinapakita lang nito ang 'Hindi Nakarehistrong Lokasyon' (tingnan ang Variant 7B). Kapag ang huling aktibidad ay CHECK-IN at ang user ay malapit ngunit nasa labas ng lugar, iba ang kilos: gumagawa ang app ng check-in na may 'Hindi Nakarehistrong Lokasyon' bilang pagbabago (tingnan ang Sitwasyon 5).\n" +
            "\n" +
            "## Sitwasyon 4 — Bagong check-in tuwing may pagbabago ng lokasyon lamang\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ang user ay nasa isang nakarehistrong lugar maliban sa 'Check-Out Zone'.\n" +
            "• Gumagawa ang app ng bagong check-in KUNG ang lugar ay IBA sa huling check-in.\n" +
            "• Sa PAREHONG lugar ng huling check-in, WALANG aksyon (inaalis nito ang dobleng check-in). Kapag lumipat ng lugar, ire-record/ia-update ng bagong check-in ang lokasyon at oras.\n" +
            "\n" +
            "## Sitwasyon 5 — Malapit ngunit nasa labas ng lugar (pagpapatuloy)\n" +
            "• Ang huling aktibidad ay check-in.\n" +
            "• Ang user ay wala sa anumang nakarehistrong lugar, ngunit malapit (wala pang 2 km mula sa isang coordinate, maliban sa Check-Out Zone).\n" +
            "• Dahil umalis siya sa lugar, gumagawa ang app ng check-in na may 'Hindi Nakarehistrong Lokasyon', nire-record ang pagpapatuloy ng paglalakbay.\n" +
            "• Nangyayari lang ito bilang PAGBABAGO: kung ang huling check-in ay 'Hindi Nakarehistrong Lokasyon' na, WALANG aksyon (hindi inuulit).\n" +
            "\n" +
            "## Sitwasyon 6 — Button na 'I-refresh' pagkatapos ng check-in\n" +
            "• Nasa foreground; ang huling aktibidad ay check-in.\n" +
            "• I-tap ng user ang 'I-refresh'.\n" +
            "• Bagong check-in KUNG ang lokasyon ay IBA sa huling check-in (parehong panuntunan ng Sitwasyon 4). Sa PAREHONG lugar, WALANG aksyon.\n" +
            "\n" +
            "## Sitwasyon 7 — Pag-alis sa Check-Out Zone\n" +
            "• Nasa foreground; ang huling aktibidad ay check-out; ang user ay nasa 'Check-Out Zone' (walang aksyon).\n" +
            "• I-tap ng user ang 'I-refresh' at ina-update ng app ang lokasyon sa: Variant 7A — isang nakarehistrong lugar maliban sa 'Check-Out Zone'; Variant 7B — walang nakarehistrong lugar, ngunit malapit pa rin (wala pang 2 km, maliban sa Check-Out Zone).\n" +
            "• 7A: dahil ang huling aktibidad ay check-out, agad na nagche-check-in ang app sa katugmang lugar.\n" +
            "• 7B: dahil naka-check-out ang user at WALA sa loob ng anumang lugar, HINDI nagche-check-in ang app; ipinapakita lang nito ang 'Hindi Nakarehistrong Lokasyon' (parehong panuntunan ng tala sa Sitwasyon 3).\n" +
            "\n" +
            "## Sitwasyon 8 — Mixed Zone\n" +
            "• Natutukoy ng app ang 'Mixed Zone' at, kung ang huling kaugnay na aktibidad ay hindi rito, agad na lumilipat: 8A — pagkatapos ng check-in → check-out sa 'Mixed Zone'; 8B — pagkatapos ng check-out → check-in sa 'Mixed Zone'.\n" +
            "• Ang 'Time Interval ng Mixed Zone' ang cooldown para sa magkasunod na pagbasa sa Mixed Zone lamang: habang nakalipas_na_oras < interval, naka-block ang bagong paglipat; kapag >= interval, pinapayagan itong muli.\n" +
            "• Mga agarang eksepsyon (isinasawalang-bahala ang cooldown): pagpunta sa 'Check-Out Zone' o lampas sa pinakamababang distansya → check-out; pagpunta sa ibang nakarehistrong lugar o pananatili sa loob ng pinakamababang distansya → check-in.\n" +
            "\n" +
            "## Sitwasyon 9 — Manu-manong mode (naka-off ang Awtomatikong mga Aktibidad)\n" +
            "• Naka-authenticate ang user; NAKA-OFF ang Awtomatikong mga Aktibidad.\n" +
            "• Ina-update ng app ang lokasyon kung may pahintulot; kung wala, ipinapakita nito ang 'Tinanggihan ang pahintulot'.\n" +
            "• Pinipili ng user ang check-in/check-out, Normal/Retroactive, pinipili ang 'Lokasyon', at i-tap ang 'I-record'.\n" +
            "• Sinusunod ng app ang normal na daloy ayon sa mga pinili.",
        "notesTitle" to "Pangkalahatang mga tala",
        "notesBody" to "## Trigger sa foreground\n" +
            "• Ang pagbubukas ng app o pagdadala nito sa foreground, na naka-on ang Awtomatikong mga Aktibidad at naka-authenticate ang user, ay nagti-trigger ng awtomatikong pagsusuri (nagpapasya ang engine ng check-in o check-out ayon sa mga sitwasyon). Pareho itong totoo para sa geofencing at sa pana-panahong pagsusuri bawat 15 minuto.\n" +
            "• Walang 'bulag' na pana-panahong check-in: palaging vine-verify ng 15-minutong pagsusuri ang lokasyon at pinapanatili ang panuntunang 'laktawan kung walang nagbago'.\n" +
            "\n" +
            "## Check-in tuwing may pagbabago ng lokasyon lamang\n" +
            "• Nangyayari lang ang awtomatikong check-in kapag ang naresolbang lokasyon ay IBA sa huling check-in. Parehong lokasyon → walang aksyon. Ang panuntunang ito (Sitwasyon 4 at 6) ang nag-aalis ng dobleng check-in.\n" +
            "\n" +
            "## FORMS bawat proyekto\n" +
            "• Sa unang check-in ng araw at sa bawat check-out, pinupunan at isinusumite ang form NANG ISANG BESES BAWAT PROYEKTO na pinagrehistruhan ng user (iginagalang ang 'naka-enable na forms' ng bawat proyekto). Hal.: user sa mga proyektong P80 at P83 → dalawang pagsusumite. User na may isang proyekto → isang pagsusumite.\n" +
            "\n" +
            "## Mga invariant ng check-out (pinanatili)\n" +
            "• Nangyayari ang awtomatikong check-out sa lahat ng inilarawang kaso (Check-Out Zone, distansyang lampas sa limitasyon, paglipat ng Mixed Zone); walang dalawang magkasunod na check-out; pagkatapos ng check-out, ang susunod na awtomatikong aktibidad ay palaging check-in.",
    ),
)

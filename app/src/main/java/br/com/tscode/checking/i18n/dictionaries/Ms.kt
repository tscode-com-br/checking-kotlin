package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun msDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Manual Checking",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "Borang daftar daftar masuk dan daftar keluar",
        "credentialsAria" to "Pengenalan pengguna dan kata laluan",
        "keyLabel" to "Kunci",
        "passwordLabel" to "Kata Laluan",
        "keyPlaceholder" to "Cth.: HR70",
        "passwordPlaceholder" to "3 hingga 10 aksara",
        "requestRegistrationButton" to "Minta pendaftaran",
        "settingsSpacer" to "Tetapan",
        "openSettingsAria" to "Buka tetapan",
        "openSettingsTitle" to "Buka tetapan",
        "waitingAuthentication" to "Menunggu pengesahan.",
        "enterPasswordPrompt" to "Masukkan kata laluan anda untuk mula.",
        "createPasswordPrompt" to "Masukkan kunci anda dan cipta kata laluan.",
        "invalidFourCharacterKey" to "Masukkan kunci alfanumerik 4 aksara.",
        "unknownUserDetail" to "Kunci pengguna tidak berdaftar",
        "transportAccessPrompt" to "Masukkan kunci anda dan sahkan kata laluan untuk mengakses Pengangkutan.",
    ),
    "history" to d(
        "lastCheckinLabel" to "Check-In Terakhir",
        "lastCheckoutLabel" to "Check-Out Terakhir",
        "today" to "Hari Ini",
        "yesterday" to "Semalam",
        "dialogTitleCheckin" to "Sejarah Check-In",
        "dialogTitleCheckout" to "Sejarah Check-Out",
        "colDate" to "Tarikh",
        "colTime" to "Masa",
        "colLocal" to "Lokasi",
        "empty" to "Tiada rekod ditemui.",
        "back" to "Kembali",
        "loadingMessage" to "Memeriksa sejarah...",
        "notFoundMessage" to "Tiada rekod ditemui untuk kunci ini.",
        "noRecordsMessage" to "Tiada check-in atau check-out direkodkan untuk kunci ini.",
        "updatedMessage" to "Sejarah telah dikemas kini untuk kunci yang dimasukkan.",
        "loadFailed" to "Tidak dapat menyemak sejarah untuk kunci ini.",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "Aktiviti Automatik",
        "sectionTitle" to "Pendaftaran",
        "checkinLabel" to "Check-In",
        "checkoutLabel" to "Check-Out",
        "transportLabel" to "Pengangkutan",
        "informeTitle" to "Jenis",
        "informeNormalLabel" to "Normal",
        "informeRetroativoLabel" to "Retroaktif",
        "submitButton" to "Daftar",
        "checkInLowerLabel" to "check-in",
        "checkOutLowerLabel" to "check-out",
        "disableAutomaticActivitiesForManualSubmit" to "Nyahaktifkan Aktiviti Automatik untuk mendaftar secara manual.",
        "selectLocationBeforeSubmit" to "Pilih lokasi sebelum mendaftar.",
    ),
    "settings" to d(
        "title" to "Tetapan",
        "languageLabel" to "Bahasa",
        "resetPasswordLabel" to "Tukar Kata Laluan",
        "allowLocationLabel" to "Benarkan Lokasi",
        "allowAudioVideoLabel" to "Benarkan Audio & Video",
        "instructionsLabel" to "Arahan",
        "supportLabel" to "Sokongan",
        "manualLabel" to "Manual penuh",
        "aboutLabel" to "Perihal",
        "backButton" to "Kembali",
    ),
    "passwordDialog" to d(
        "titleChange" to "Tukar Kata Laluan",
        "titleRegister" to "Cipta Kata Laluan",
        "oldPasswordLabel" to "Kata Laluan Lama",
        "newPasswordLabel" to "Kata Laluan Baharu",
        "confirmPasswordLabel" to "Sahkan Kata Laluan",
        "backButton" to "Kembali",
        "submitChangeButton" to "Tukar",
        "submitRegisterButton" to "Simpan",
        "changingStatus" to "Sedang menukar kata laluan...",
        "savingStatus" to "Sedang menyimpan kata laluan...",
        "validatingStatus" to "Sedang mengesahkan kata laluan.",
        "oldPasswordInvalid" to "Kata laluan lama mesti antara 3 hingga 10 aksara.",
        "newPasswordInvalid" to "Kata laluan baharu mesti antara 3 hingga 10 aksara.",
        "confirmMismatch" to "Pengesahan kata laluan baharu tidak sepadan.",
        "changeFailed" to "Tidak dapat menukar kata laluan.",
        "validationFailed" to "Tidak dapat mengesahkan kata laluan.",
        "statusLoadFailed" to "Tidak dapat menyemak status kata laluan.",
    ),
    "registrationDialog" to d(
        "title" to "Minta Pendaftaran",
        "note" to "Isi maklumat di bawah untuk menggunakan sistem Checking.",
        "keyLabel" to "Kunci",
        "fullNameLabel" to "Nama Penuh",
        "projectsLabel" to "Projek",
        "projectsHint" to "Pilih satu atau lebih projek.",
        "emailLabel" to "E-Mel",
        "emailPlaceholder" to "Pilihan",
        "passwordLabel" to "Kata Laluan",
        "confirmPasswordLabel" to "Sahkan Kata Laluan",
        "backButton" to "Kembali",
        "submitButton" to "Hantar",
        "loadingProjects" to "Memuatkan projek...",
        "noProjectsAvailable" to "Tiada projek tersedia sekarang.",
        "fullNameRequired" to "Masukkan nama penuh.",
        "emailInvalid" to "Masukkan e-mel yang sah atau biarkan medan kosong.",
        "passwordInvalid" to "Kata laluan mesti antara 3 hingga 10 aksara.",
        "confirmMismatch" to "Pengesahan kata laluan baharu tidak sepadan.",
        "submittingStatus" to "Menghantar permintaan pendaftaran...",
        "successStatus" to "Pendaftaran berjaya diselesaikan.",
        "submitFailed" to "Tidak dapat menghantar permintaan pendaftaran.",
    ),
    "location" to d(
        "title" to "Lokasi",
        "waitingLabel" to "Menunggu lokasi.",
        "refreshLabel" to "Segar semula lokasi",
        "refreshBusyLabel" to "Sedang menyegar semula lokasi",
        "unavailableShort" to "Tidak Tersedia",
        "unavailableLabel" to "Lokasi tidak tersedia",
        "unavailableMessage" to "Tidak dapat menyemak lokasi sekarang.",
        "noPermissionLabel" to "Tiada Kebenaran",
        "timeoutLabel" to "Masa Tamat",
        "timeoutMessage" to "Carian lokasi mengambil masa lebih lama daripada jangkaan.",
        "detectingLabel" to "Mengesan...",
        "exactConfirmationBrowser" to "Menunggu pengesahan lokasi tepat daripada pelayar.",
        "exactConfirmationApp" to "Menunggu pengesahan lokasi tepat daripada pintasan/aplikasi.",
        "updatingDeviceLocation" to "Mengemas kini lokasi semasa peranti.",
        "secureContextRequired" to "Lokasi tepat memerlukan sambungan selamat (HTTPS).",
        "browserUnsupported" to "Pelayar ini tidak menyokong lokasi tepat.",
        "permissionBlocked" to "Kebenaran lokasi disekat dalam pelayar. Benarkan akses tapak dalam tetapan pelayar.",
        "captureRequiresSupport" to "Pengambilan lokasi memerlukan HTTPS dan sokongan pelayar.",
        "noValidPosition" to "Tidak dapat mendapatkan kedudukan peranti yang sah.",
        "searchingPrecision" to "Mencari ketepatan yang mencukupi...",
        "completionStatus" to "Kemas kini lokasi selesai.",
        "completionStatusWithDetail" to "Kemas kini lokasi selesai. {detail}",
        "browserContextLabel" to "dalam pelayar ini",
        "appContextLabel" to "dalam pintasan/aplikasi ini",
        "browserSourceLabel" to "melalui pelayar",
        "appSourceLabel" to "melalui pintasan/aplikasi",
        "currentAccuracyLabel" to "Ketepatan semasa",
        "accuracyPrefix" to "Ketepatan",
        "accuracyTemplate" to "Ketepatan {accuracy}",
        "accuracyLimitTemplate" to "Had {limit} m",
        "accuracyCombinedTemplate" to "Ketepatan {accuracy} / Had {limit} m",
        "noKnownLocations" to "Tiada lokasi berdaftar",
        "defaultManualLocationLabel" to "Pejabat Utama",
        "accuracyFallbackManualLocationLabel" to "Ketepatan Tidak Mencukupi",
        "outsideWorkplaceLabel" to "Di Luar Tempat Kerja",
        "unregisteredLocationLabel" to "Lokasi Tidak Berdaftar",
        "mixedZoneLabel" to "Zon Campuran",
        "checkoutZoneLabel" to "Zon daftar keluar",
    ),
    "projects" to d(
        "label" to "Projek",
        "changeButton" to "Tukar",
        "loadingProjects" to "Memuatkan projek...",
        "updatingProjects" to "Mengemas kini projek...",
        "noneAvailableShort" to "Tiada projek tersedia",
        "noneAvailableSentence" to "Tiada projek tersedia.",
        "noneAvailableNow" to "Tiada projek tersedia sekarang.",
        "selectAtLeastOne" to "Pilih sekurang-kurangnya satu projek.",
        "userProjectsAria" to "Projek pengguna",
        "registrationProjectsAria" to "Projek pendaftaran",
        "updatedSuccess" to "Projek berjaya dikemas kini.",
        "loadFailed" to "Tidak dapat memuatkan projek.",
        "userProjectsLoadFailed" to "Tidak dapat memuatkan projek pengguna.",
        "updateFailed" to "Tidak dapat mengemas kini projek.",
    ),
    "transport" to d(
        "title" to "Penjadualan Pengangkutan",
        "backToMainAria" to "Kembali ke skrin utama",
        "addressToggleLabel" to "Alamat:",
        "addressLabel" to "Alamat:",
        "zipLabel" to "Kod ZIP:",
        "addressPlaceholder" to "Blok (jika ada), jalan dan nombor.",
        "zipPlaceholder" to "Hanya 6 digit",
        "addressBackButton" to "Kembali",
        "addressSubmitButton" to "Simpan",
        "optionInstruction" to "Pilih jenis pengangkutan untuk teruskan.",
        "historyTitle" to "Permintaan aktif",
        "historyButtonLabel" to "Sejarah",
        "historyPanelTitle" to "Sejarah Permintaan",
        "historyCloseButton" to "Tutup",
        "kinds" to d(
            "regular" to "Hari Bekerja",
            "weekend" to "Hujung Minggu",
            "extra" to "Tarikh Tertentu",
        ),
        "statusLabels" to d(
            "available" to "Tiada permintaan",
            "pending" to "Menunggu",
            "confirmed" to "Disahkan",
            "realized" to "Selesai",
            "rejected" to "Ditolak",
            "cancelled" to "Dibatalkan",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "Isn",
                "1" to "Sel",
                "2" to "Rab",
                "3" to "Kha",
                "4" to "Jum",
                "5" to "Sab",
                "6" to "Aha",
            ),
            "full" to d(
                "0" to "Isnin",
                "1" to "Selasa",
                "2" to "Rabu",
                "3" to "Khamis",
                "4" to "Jumaat",
                "5" to "Sabtu",
                "6" to "Ahad",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "Pilih hari:",
            "regularSubtitle" to "Pilih hari bekerja yang anda mahu untuk permintaan ini.",
            "weekendSubtitle" to "Pilih hari hujung minggu yang anda mahu untuk permintaan ini.",
            "extraSubtitle" to "Semak tarikh dan masa sebelum meminta.",
            "dateLabel" to "Tarikh:",
            "timeLabel" to "Masa:",
            "backButton" to "Kembali",
            "submitButton" to "Minta",
            "requestUnavailable" to "Permintaan pengangkutan tidak tersedia.",
            "addressRequired" to "Daftarkan alamat lengkap sebelum meminta pengangkutan.",
            "dateRequiredExtra" to "Masukkan tarikh untuk pengangkutan tambahan.",
            "timeRequiredExtra" to "Masukkan masa untuk pengangkutan tambahan.",
            "dayRequired" to "Pilih sekurang-kurangnya satu hari untuk meminta pengangkutan.",
            "conflictGeneric" to "Sudah ada permintaan pengangkutan aktif untuk tarikh itu.",
            "conflictByDate" to "Sudah ada permintaan pengangkutan aktif untuk {serviceDateLabel}.",
        ),
        "summary" to d(
            "noRequestRecorded" to "Tiada permintaan direkodkan.",
            "noActiveRequests" to "Tiada permintaan aktif.",
            "noRequestStatus" to "Tiada permintaan",
            "waitingAllocation" to "Menunggu peruntukan pengangkutan.",
            "vehicleAllocated" to "Kenderaan diperuntukkan.",
            "scheduleUnavailable" to "Jadual tidak tersedia.",
            "requestClosed" to "Permintaan ditutup.",
            "whenRequestExists" to "Apabila ada permintaan, ia akan dipaparkan di sini.",
            "whenAllocated" to "Apabila anda diperuntukkan kepada kenderaan, maklumat akan muncul di sini.",
            "departureAndLimit" to "Berlepas {departureTime} • Had {deadlineTime}",
            "limitOnly" to "Had {deadlineTime}",
        ),
        "detail" to d(
            "title" to "Butiran Permintaan",
            "genericTitle" to "Pengangkutan",
            "waitingAllocation" to "Menunggu peruntukan pengangkutan.",
            "whenAllocated" to "Apabila anda diperuntukkan kepada kenderaan, maklumat akan muncul di sini.",
            "inactive" to "Permintaan ini tidak lagi aktif.",
            "confirmed" to "Pengangkutan disahkan.",
            "realized" to "Pengangkutan selesai.",
            "vehicleTypeLabel" to "Jenis Kenderaan",
            "vehiclePlateLabel" to "Nombor Plat",
            "vehicleColorLabel" to "Warna Kenderaan",
            "departureDateLabel" to "Tarikh Berlepas",
            "departureTimeLabel" to "Masa Berlepas",
            "unavailableValue" to "Tidak tersedia",
        ),
        "actions" to d(
            "markRealized" to "Selesai",
            "cancel" to "Batal",
            "cancelling" to "Sedang membatalkan...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "Masukkan kunci yang sah sebelum mengemas kini alamat.",
            "invalidKeyBeforeRequest" to "Masukkan kunci yang sah sebelum meminta pengangkutan.",
            "requestFailed" to "Tidak dapat meminta {requestLabel}.",
            "loadFailed" to "Tidak dapat menyemak pengangkutan.",
            "addressUpdated" to "Alamat berjaya dikemas kini.",
            "addressUpdateFailed" to "Tidak dapat mengemas kini alamat.",
            "cancelSuccess" to "Permintaan pengangkutan dibatalkan.",
            "cancelFailed" to "Tidak dapat membatalkan permintaan.",
            "requestMarkedRealized" to "Permintaan ditandakan sebagai selesai.",
            "accessRequiresAuthentication" to "Masukkan kunci anda dan sahkan kata laluan untuk mengakses Pengangkutan.",
        ),
    ),
    "status" to d(
        "validationError" to "Ralat pengesahan.",
        "apiCommunicationFailure" to "Komunikasi API gagal.",
        "passwordVerifying" to "Sedang mengesahkan kata laluan.",
        "authenticationCompleted" to "Pengesahan selesai. Sedang mengemas kini aplikasi...",
        "userAuthenticated" to "Pengguna disahkan. Memulakan kemas kini.",
        "applicationUpdated" to "Aplikasi berjaya dikemas kini.",
        "applicationUpdateFailed" to "Tidak dapat mengemas kini aplikasi sekarang.",
        "checkinCompleted" to "Check-In selesai.",
        "checkoutCompleted" to "Check-Out selesai.",
        "savedOffline" to "Disimpan luar talian. Akan disegerakkan apabila sambungan kembali.",
        "automaticCheckinCompleted" to "Check-In automatik selesai.",
        "automaticCheckoutCompleted" to "Check-Out automatik selesai.",
        "updatingActivitiesSequence" to "Mengemas kini aktiviti.....",
        "updatingLocationSequence" to "Mengemas kini lokasi.....",
        "runningAutomaticActivitySequence" to "Menjalankan check-in atau check-out apabila perlu.....",
        "automaticUpdatesRunning" to "Kemas kini sedang berjalan.",
        "automaticUpdatesCompletedWithActivity" to "Kemas kini selesai dengan {activity} dilakukan.",
        "automaticUpdatesCompletedWithoutActivity" to "Kemas kini selesai tanpa sebarang aktiviti dilakukan.",
        "automaticUpdatesFailed" to "Tidak dapat menyelesaikan kemas kini automatik sekarang.",
        "automaticActivitiesDisabled" to "Mod 100% manual telah diaktifkan.",
        "operationFailed" to "Tidak dapat menyelesaikan operasi.",
    ),
    "manual" to d(
        "heading" to "Manual Checking Web",
        "introPrimary" to "Halaman ini ialah titik masuk manual yang stabil yang digunakan oleh Tetapan > Perihal.",
        "introSecondary" to "Manual penuh dengan tangkapan skrin akan ditambah dalam fasa pelaksanaan seterusnya.",
    ),
    "accident" to d(
        "button" to d(
            "report" to "Laporkan Kemalangan",
            "reported" to "Kemalangan Dilaporkan",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "Saya perlukan bantuan dengan aplikasi Web. Kunci saya ialah {chave}.",
    ),
    "instructions" to d(
        "heading" to "Arahan",
        "intro" to "Panduan ini menunjukkan, langkah demi langkah, cara menggunakan Checking: merekod kehadiran secara manual, menghidupkan Mod Automatik (check-in dan check-out mengikut lokasi) dan menetapkan Jeda Berjadual.",
        "step1" to d(
            "title" to "1. Log masuk ke aplikasi",
            "item1" to "Pada skrin utama, masukkan kunci 4 aksara anda dalam medan 'Kunci'. Medan bertukar jingga apabila kunci ditemui.",
            "item2" to "Masukkan kata laluan anda dalam medan 'Kata Laluan'. Setelah disahkan, medan bertukar hijau dan 'Pengesahan selesai' dipaparkan.",
            "item3" to "Jika anda belum mempunyai kata laluan, aplikasi membuka penciptaan kata laluan secara automatik; jika kunci tidak wujud, ia menawarkan pendaftaran sendiri.",
        ),
        "step2" to d(
            "title" to "2. Rekod kehadiran secara manual",
            "item1" to "Pilih 'Check-In' atau 'Check-Out' dan jenis 'Biasa' atau 'Retroaktif'.",
            "item2" to "Dengan Mod Automatik dimatikan, pilih 'Lokasi' dalam senarai dan ketik 'Rekod Check-In' (atau 'Check-Out').",
            "item3" to "Kad di bahagian atas memaparkan check-in dan check-out terakhir anda; ketik padanya untuk melihat senarai penuh dengan tarikh, masa dan lokasi.",
        ),
        "step3" to d(
            "title" to "3. Hidupkan Mod Automatik",
            "lead" to "Dengan Mod Automatik, aplikasi melakukan check-in dan check-out sendiri berdasarkan lokasi anda — semasa memasuki atau meninggalkan kawasan berdaftar, semasa membawa aplikasi ke latar depan dan pada semakan berkala.",
            "item1" to "Ketik ikon gear (di sebelah medan kunci/kata laluan) untuk membuka 'Tetapan'.",
            "item2" to "Ketik 'Aktiviti Automatik' dan tandai kotak 'Hidupkan Aktiviti Automatik'.",
            "item3" to "Berikan setiap kebenaran dalam senarai yang muncul dengan mengetiknya: Pemberitahuan, Lokasi 'Sentiasa benarkan', penggunaan Bateri tanpa had dan — pada sesetengah peranti — 'Mula dengan peranti'.",
            "item4" to "Apabila gear bersinar HIJAU, Mod Automatik aktif dan sihat. Sinaran JINGGA menunjukkan satu kebenaran yang disyorkan masih kurang.",
            "callout" to "Penting: untuk berfungsi dengan boleh dipercayai di latar belakang, berikan Lokasi sebagai 'Sentiasa benarkan' dan matikan pengoptimuman bateri untuk Checking.",
        ),
        "step4" to d(
            "title" to "4. Hidupkan Jeda Berjadual",
            "lead" to "Jeda Berjadual menjimatkan bateri dengan menjeda aktiviti automatik dalam suatu tempoh (contohnya pada waktu malam).",
            "item1" to "Dalam 'Tetapan', ketik 'Jeda Berjadual'.",
            "item2" to "Hidupkan pilihan itu dan tetapkan masa 'Dari' dan 'Hingga' (contohnya 22:00 hingga 06:00).",
            "item3" to "Jika mahu, tandai juga 'Jeda pada Sabtu' dan/atau 'Jeda pada Ahad'.",
            "item4" to "Semasa jeda, aplikasi tidak melakukan sebarang aktiviti automatik; ia disambung semula sendiri pada akhir tempoh.",
        ),
        "step5" to d(
            "title" to "5. Pantau sejarah",
            "item1" to "Ketik 'CHECK-IN TERAKHIR' atau 'CHECK-OUT TERAKHIR' untuk membuka jadual dengan Tarikh, Masa dan Lokasi setiap rekod.",
            "item2" to "Rekod yang dibuat berhampiran tetapi di luar kawasan berdaftar dipaparkan sebagai 'Lokasi Tidak Berdaftar'.",
            "item3" to "Walaupun tanpa internet, rekod anda disimpan dalam peranti dan dihantar sebaik sahaja sambungan kembali, sentiasa dengan masa asal.",
        ),
        "step6" to d(
            "title" to "6. Minta pengangkutan",
            "item1" to "Ketik 'Pengangkutan' untuk membuka modul pengangkutan kakitangan.",
            "item2" to "Masukkan alamat dan masa yang diperlukan dan hantar permintaan.",
            "item3" to "Pengurus logistik menyusun perjalanan; enjin kecerdasan buatan mencadangkan cara mengumpulkan penumpang dan menyusun perhentian.",
        ),
        "step7" to d(
            "title" to "7. Sekiranya berlaku kemalangan",
            "lead" to "Mod Kemalangan ialah ciri keselamatan. Gunakannya hanya dalam kecemasan sebenar.",
            "item1" to "Mana-mana pengguna boleh membuka Mod Kemalangan; ia memberitahu, secara masa nyata, semua pengguna dalam projek yang sama.",
            "item2" to "Laporkan keadaan dan zon anda: 'selamat', 'di lokasi kemalangan tetapi selamat', atau 'di lokasi kemalangan dan memerlukan bantuan'.",
            "item3" to "Jika boleh, rakam video lokasi: ia dihantar secara masa nyata ke papan pemuka pentadbir.",
            "item4" to "Butang 'Hubungi Perkhidmatan Kecemasan' menghubungi perkhidmatan kecemasan tempatan, melaporkan kemalangan dan lokasi dalam bahasa wilayah berkenaan.",
        ),
        "step8" to d(
            "title" to "8. Tetapan lain",
            "item1" to "'Makluman': pilih pemberitahuan yang anda terima (aktiviti, jeda berjadual, kemalangan).",
            "item2" to "'Bahasa': tukar bahasa aplikasi.",
            "item3" to "'Tukar Kata Laluan': tetapkan kata laluan baharu apabila perlu.",
            "item4" to "'Sokongan': bercakap terus dengan pasukan melalui WhatsApp.",
            "item5" to "'Perihal': ketahui sejarah Checking dan bahagian yang membentuk sistem.",
        ),
        "closing" to "Selesai! Dengan Mod Automatik dihidupkan, anda tidak perlu merekod apa-apa secara manual — Checking menguruskannya untuk anda.",
    ),
    "about" to d(
        "heading" to "Perihal Checking",
        "introTitle" to "Bagaimana Checking bermula",
        "introBody" to "Checking mula dibangunkan pada Mac 2026, hasil idea Jurutera Dilnei Schmidt.\n" +
            "\n" +
            "Terdapat keperluan untuk mengenal pasti dengan cepat setiap pekerja Petrobras yang berada di tapak kerja pembinaan dan pemasangan, sekiranya berlaku kemalangan.\n" +
            "\n" +
            "Penyelesaian pertama pihak pengurusan SMS ialah borang dalam talian, diisi oleh setiap pekerja semasa tiba dan meninggalkan tapak kerja. Ia berkesan untuk mengenal pasti siapa yang hadir, tetapi memenatkan dan ramai yang kadangkala terlupa mengisinya.\n" +
            "\n" +
            "Untuk meningkatkan kecekapan, Dilnei membina aplikasi yang mampu:\n" +
            "• mengenal pasti, melalui GPS, kehampiran pengguna dengan tapak kerja dan mengingatkannya untuk check-in;\n" +
            "• menetapkan penggera pada masa check-in dan check-out yang lazim, mengingatkan pengguna mengisi borang;\n" +
            "• mengisi borang secara automatik dengan data pengguna dan menghantarnya dalam talian.\n" +
            "\n" +
            "Ini memudahkan kerja dan meningkatkan kekerapan pengisian borang.\n" +
            "\n" +
            "Masih pada Mac 2026, Jurutera Tamer Salmem mengetahui penyelesaian yang telah dilaksanakan dan memajukan penggunaan teknologi pengaturcaraan terkini, membangunkan sistem yang mula-mula diidamkan oleh Dilnei.\n" +
            "\n" +
            "Matlamatnya ialah supaya pengguna tidak perlu risau membuka aplikasi untuk check-in atau check-out. Selain itu, membina pemantauan masa nyata supaya pentadbir tahu bukan sahaja siapa yang berada di tempat kerja, malah di lokasi berdaftar yang mana bagi setiap projek setiap pengguna berada — meningkatkan keupayaan bertindak balas dalam kecemasan.\n" +
            "\n" +
            "Maka, sistem memperoleh:\n" +
            "• pengaktifan perkhidmatan melalui geofencing (berdasarkan kehampiran pengguna dengan tapak kerja);\n" +
            "• pelaksanaan tugas di latar belakang — check-in pada setiap perubahan lokasi dalam kemudahan dan check-out apabila pengguna menjauh, tanpa pengguna perlu membuka kunci peranti;\n" +
            "• penghantaran lokasi pengguna secara masa nyata ke papan pemuka pentadbir;\n" +
            "• keupayaan mendaftarkan seberapa banyak projek yang diperlukan, di mana-mana sahaja di dunia.\n" +
            "\n" +
            "Sistem ini juga boleh memasuki 'Mod Kemalangan'. Sekiranya berlaku kemalangan, mana-mana pengguna boleh mencetuskan penggera yang memberitahu, secara masa nyata, semua pengguna dalam projek yang sama. Dengan Mod Kemalangan aktif:\n" +
            "• sebuah jadual dicipta pada papan pemuka pentadbir, menyenaraikan keadaan setiap pengguna: 'selamat', 'di lokasi kemalangan tetapi selamat', dan 'di lokasi kemalangan dan memerlukan bantuan';\n" +
            "• pengguna boleh merakam video dan menghantarnya secara masa nyata, sebagai pautan dalam jadual, supaya pentadbir melihat rakaman lokasi;\n" +
            "• butang 'Hubungi Perkhidmatan Kecemasan' menghubungi perkhidmatan kecemasan tempatan yang berdaftar, melaporkan kemalangan, lokasi dan orang yang boleh dihubungi, bertutur dalam bahasa wilayah berkenaan.\n" +
            "\n" +
            "Keteguhan dan kebolehpercayaan sistem membawa keselamatan operasi dan tindak balas segera kepada pasukan SMS Petrobras.\n" +
            "\n" +
            "Akhir sekali, Jurutera Thiago Soares do Nascimento menyepadukan maklumat yang dihasilkan sistem dengan papan pemuka pengurusan sedia ada, supaya sistem baharu berfungsi seiring dengan pengisian borang lama, mengekalkan kawalan pengurusan sentiasa dikemas kini.\n" +
            "\n" +
            "Begitulah CHECKING dilahirkan.",
        "partsTitle" to "Bahagian-bahagian sistem",
        "partsIntro" to "Checking ialah sistem kawalan kehadiran yang merekodkan kemasukan dan keluar pekerja di tapak kerja. Ia berfungsi melalui pelbagai saluran — pembaca kad RFID yang dipasang di tapak, aplikasi Android, halaman web yang boleh diakses dari telefon, dan panel pentadbiran — menghimpunkan semuanya di satu tempat.\n" +
            "\n" +
            "Ia terdiri daripada:\n" +
            "• sebuah API, dibina dengan Python/FastAPI;\n" +
            "• sebuah laman web untuk pentadbir sistem;\n" +
            "• sebuah aplikasi Web, responsif untuk telefon dan desktop;\n" +
            "• sebuah papan pemuka untuk pengangkutan kakitangan;\n" +
            "• sebuah aplikasi khusus Android, dibina dengan Kotlin.",
        "partApiTitle" to "API",
        "partApiBody" to "API ialah otak sistem. Setiap kali seseorang check-in atau check-out — melalui pembaca fizikal, aplikasi atau halaman web — ia menerima maklumat itu, mengesahkan ia betul, menyimpannya ke pangkalan data, dan memberitahu komponen lain secara masa nyata.\n" +
            "\n" +
            "Ia juga mengisi borang korporat Microsoft Forms secara automatik selepas setiap rekod, menyelaras sistem pengangkutan, mencetuskan amaran kecemasan sekiranya berlaku kemalangan, dan memastikan tiada data hilang apabila sambungan tidak stabil.",
        "partWebsiteTitle" to "Laman web",
        "partWebsiteBody" to "Laman web ialah panel kawalan pentadbir. Melaluinya, anda boleh melihat secara masa nyata siapa yang check-in dan siapa yang check-out, serta mengurus setiap aspek sistem tanpa pengetahuan teknikal.\n" +
            "\n" +
            "Fungsi utama: mendaftar dan menyunting pekerja, mencipta projek dan peraturannya, menentukan kawasan geografi yang dikenali sistem, melihat laporan kehadiran, dan mengeksport data. Ia juga titik pusat untuk mencetus dan memantau Mod Kemalangan — melihat keadaan setiap pekerja secara masa nyata dan menyelaras tindak balas kecemasan.",
        "partWebappTitle" to "Aplikasi Web",
        "partWebappBody" to "Aplikasi web ialah alat pekerja. Ia berjalan dalam pelayar telefon atau komputer, tanpa perlu memasang apa-apa, dan membenarkan setiap orang merekod masuk atau keluar, melihat sejarah mereka, dan meminta pengangkutan.\n" +
            "\n" +
            "Apabila pekerja menghidupkan Aktiviti Automatik, telefon itu sendiri mengesan lokasi dan check-in atau check-out secara automatik semasa memasuki atau meninggalkan kawasan berdaftar. Sekiranya berlaku kemalangan, antara muka berubah dan meminta pekerja melaporkan keadaan dan zon keselamatannya.\n" +
            "\n" +
            "Ia tersedia dalam enam bahasa (Portugis, Inggeris, Cina, Melayu, Indonesia, dan Tagalog) untuk pasukan antarabangsa.",
        "partTransportTitle" to "Papan pemuka pengangkutan",
        "partTransportBody" to "Papan pemuka pengangkutan ialah alat bagi orang yang menguruskan logistik perjalanan. Melaluinya, anda boleh mendaftarkan kenderaan, melihat dan menyusun permintaan pengangkutan yang dibuat oleh pekerja, dan menetapkan setiap orang kepada kenderaan untuk hari itu.\n" +
            "\n" +
            "Ia merangkumi enjin kecerdasan buatan yang menganalisis alamat dan masa dan mencadangkan secara automatik cara mengumpulkan penumpang dan menyusun perhentian dengan cekap — mengurangkan masa perjalanan dan bilangan perjalanan. Pengurus boleh menerima cadangan, melaraskannya, atau menyusun penetapan secara manual.",
        "partAndroidTitle" to "Aplikasi Android",
        "partAndroidBody" to "Aplikasi Android menawarkan fungsi yang sama seperti aplikasi web, dengan pengalaman harian yang lebih lengkap. Kelebihan utamanya ialah automasi geolokasi: aplikasi berjalan di latar belakang dan merekod check-in atau check-out secara automatik semasa pekerja memasuki dan meninggalkan kawasan berdaftar, tanpa bergantung pada pelayar.\n" +
            "\n" +
            "Ia juga berfungsi tanpa internet: tanpa sambungan, rekod disimpan dalam telefon dan dihantar sebaik sahaja sambungan kembali, sentiasa dengan masa asal. Ia turut merangkumi sejarah dengan tarikh, masa dan lokasi setiap peristiwa, modul pengangkutan, dan mod kecemasan untuk kemalangan.",
        "rulesTitle" to "Situasi check-in dan check-out",
        "rulesIntro" to "Situasi di bawah menerangkan, langkah demi langkah, apa yang sistem mesti lakukan untuk setiap pengguna (check-in atau check-out) dalam setiap senario lazim. Aplikasi Web dan Aplikasi Asli mengikut peraturan blok masing-masing.",
        "rulesWebTitle" to "Situasi — Aplikasi Web",
        "rulesWebBody" to "## Situasi 1 — Check-out apabila menjauh\n" +
            "• Aktiviti Automatik dihidupkan, dengan kebenaran lokasi penuh.\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Aplikasi mengemas kini lokasi dan mendapati pengguna berada di 'Zon Check-Out' atau lebih 2 km dari mana-mana tempat berdaftar (kecuali Zon Check-Out).\n" +
            "• Memandangkan aktiviti terakhir ialah check-in, aplikasi membuat check-out.\n" +
            "\n" +
            "## Situasi 2 — Sudah check-out, jauh atau di Zon Check-Out\n" +
            "• Aktiviti terakhir ialah check-out.\n" +
            "• Pengguna berada di 'Zon Check-Out' atau lebih 2 km dari mana-mana tempat berdaftar.\n" +
            "• Tiada tindakan: check-out tidak diulang kerana perubahan lokasi.\n" +
            "\n" +
            "## Situasi 3 — Tiba di tempat kerja (check-in)\n" +
            "• Aktiviti terakhir ialah check-out.\n" +
            "• Pengguna berada DI DALAM kawasan berdaftar selain 'Zon Check-Out' (padanan sebenar dengan kawasan, bukan sekadar berhampiran).\n" +
            "• Pengguna benar-benar berada di tempat kerja (termasuk check-in pertama hari itu).\n" +
            "• Aplikasi membuat check-in dan mengemas kini lokasi kepada kawasan berdaftar yang sepadan.\n" +
            "! PENTING: jika pengguna TIDAK berada di dalam mana-mana kawasan berdaftar — walaupun berhampiran (kurang 2 km dari sesuatu koordinat, kecuali Zon Check-Out) — aplikasi TIDAK check-in automatik; ia hanya memaparkan 'Lokasi Tidak Berdaftar' (sama seperti Situasi 5).\n" +
            "\n" +
            "## Situasi 4 — Check-in baharu (sentiasa)\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Pengguna berada di kawasan berdaftar selain 'Zon Check-Out'.\n" +
            "• Aplikasi membuat check-in baharu TANPA MENGIRA sama ada lokasi berubah.\n" +
            "• Walaupun di tempat yang SAMA dengan check-in terakhir, check-in baharu dibuat untuk merekod/mengemas kini lokasi dan masa.\n" +
            "\n" +
            "## Situasi 5 — Berhampiran tetapi di luar kawasan\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Pengguna tiada dalam mana-mana kawasan berdaftar, tetapi juga tidak lebih 2 km dari sesuatu koordinat berdaftar (kecuali Zon Check-Out). Maksudnya, pengguna berhampiran tempat kerja.\n" +
            "• Tiada tindakan: aplikasi hanya memaparkan 'Lokasi Tidak Berdaftar'.\n" +
            "\n" +
            "## Situasi 6 — Butang 'Segar Semula' selepas check-in\n" +
            "• Aplikasi sudah di latar depan; aktiviti terakhir ialah check-in.\n" +
            "• Pengguna ketik 'Segar Semula' untuk mengemas kini lokasi.\n" +
            "• Aplikasi membuat check-in baharu TANPA MENGIRA sama ada lokasi berubah, untuk merekod/mengemas kini lokasi dan masa.\n" +
            "\n" +
            "## Situasi 7 — Meninggalkan Zon Check-Out\n" +
            "• Di latar depan; aktiviti terakhir ialah check-out; pengguna di 'Zon Check-Out' (tiada tindakan).\n" +
            "• Pengguna ketik 'Segar Semula' dan aplikasi mendapati dia telah meninggalkan Zon Check-Out, kepada:\n" +
            "• Varian 7A — kawasan berdaftar selain 'Zon Check-Out';\n" +
            "• Varian 7B — tiada kawasan berdaftar, tetapi masih berhampiran (kurang 2 km, kecuali Zon Check-Out).\n" +
            "• Dalam kedua-duanya, aplikasi segera check-in, mengemas kini lokasi kepada kawasan berdaftar atau, tanpa padanan tepat, kepada 'Lokasi Tidak Berdaftar'.\n" +
            "\n" +
            "## Situasi 8 — Zon Campuran\n" +
            "• Aplikasi mengesan kedudukan sepadan dengan 'Zon Campuran' (pada kemasukan pertama atau bacaan berturutan).\n" +
            "• Jika aktiviti relevan terakhir BUKAN dalam 'Zon Campuran' itu sendiri, penukaran adalah segera: 8A — selepas check-in → check-out di 'Zon Campuran'; 8B — selepas check-out → check-in di 'Zon Campuran'.\n" +
            "• Medan 'Selang Masa Zon Campuran' (tab 'Pendaftaran' laman web pentadbir) ialah masa rehat untuk bacaan berturutan dalam Zon Campuran sahaja: selagi masa_berlalu < selang, penukaran baharu disekat; apabila >= selang, ia dibenarkan semula.\n" +
            "• Pengecualian selepas check-in di Zon Campuran: pergi ke 'Zon Check-Out' atau melepasi 'Jarak minimum untuk check-out automatik' → check-out segera, tanpa menunggu masa rehat.\n" +
            "• Pengecualian selepas check-out di Zon Campuran: pergi ke kawasan berdaftar lain (kecuali Zon Check-Out dan Zon Campuran) atau kekal dalam jarak minimum → check-in segera, membuang masa rehat.\n" +
            "\n" +
            "## Situasi 9 — Mod manual (Aktiviti Automatik dimatikan)\n" +
            "• Pengguna disahkan; Aktiviti Automatik DIMATIKAN.\n" +
            "• Aplikasi mengemas kini lokasi jika ada kebenaran; jika tidak, ia memaparkan 'Kebenaran ditolak'.\n" +
            "• Pengguna memilih 'check-in' atau 'check-out', 'Biasa' atau 'Retroaktif', memilih 'Lokasi' (tersedia bila-bila Aktiviti Automatik dimatikan), dan ketik 'Rekod'.\n" +
            "• Aplikasi mengikut aliran biasa dan melaksanakan aktiviti mengikut pilihan.",
        "rulesNativeTitle" to "Situasi — Aplikasi Asli (Android)",
        "rulesNativeBody" to "## Situasi 1 — Check-out apabila menjauh\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Pengguna di 'Zon Check-Out' atau lebih 2 km dari mana-mana tempat berdaftar (kecuali Zon Check-Out).\n" +
            "• Aplikasi membuat check-out.\n" +
            "\n" +
            "## Situasi 2 — Sudah check-out, jauh atau di Zon Check-Out\n" +
            "• Aktiviti terakhir ialah check-out.\n" +
            "• Pengguna di 'Zon Check-Out' atau lebih 2 km dari mana-mana tempat berdaftar.\n" +
            "• Tiada tindakan: check-out tidak diulang kerana perubahan lokasi.\n" +
            "\n" +
            "## Situasi 3 — Tiba di tempat kerja (check-in)\n" +
            "• Aktiviti terakhir ialah check-out.\n" +
            "• Pengguna berada DI DALAM kawasan berdaftar selain 'Zon Check-Out' (padanan sebenar, bukan sekadar berhampiran).\n" +
            "• Aplikasi membuat check-in dan mengemas kini lokasi kepada kawasan yang sepadan.\n" +
            "! PENTING: bermula daripada CHECK-OUT, jika pengguna TIDAK berada di dalam mana-mana kawasan berdaftar — walaupun berhampiran — aplikasi TIDAK check-in; ia hanya memaparkan 'Lokasi Tidak Berdaftar' (lihat Varian 7B). Apabila aktiviti terakhir ialah CHECK-IN dan pengguna berhampiran tetapi di luar kawasan, kelakuannya berbeza: aplikasi membuat check-in dengan 'Lokasi Tidak Berdaftar' sebagai perubahan (lihat Situasi 5).\n" +
            "\n" +
            "## Situasi 4 — Check-in baharu hanya pada perubahan lokasi\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Pengguna di kawasan berdaftar selain 'Zon Check-Out'.\n" +
            "• Aplikasi membuat check-in baharu HANYA jika kawasan itu BERBEZA daripada check-in terakhir.\n" +
            "• Di tempat yang SAMA dengan check-in terakhir, TIADA tindakan (ini menghapuskan check-in berganda). Apabila bertukar kawasan, check-in baharu merekod/mengemas kini lokasi dan masa.\n" +
            "\n" +
            "## Situasi 5 — Berhampiran tetapi di luar kawasan (sambungan)\n" +
            "• Aktiviti terakhir ialah check-in.\n" +
            "• Pengguna tiada dalam mana-mana kawasan berdaftar, tetapi berhampiran (kurang 2 km dari sesuatu koordinat, kecuali Zon Check-Out).\n" +
            "• Memandangkan dia meninggalkan kawasan, aplikasi membuat check-in dengan 'Lokasi Tidak Berdaftar', merekod kesinambungan perjalanan.\n" +
            "• Ia berlaku hanya sebagai PERUBAHAN: jika check-in terakhir sudah 'Lokasi Tidak Berdaftar', TIADA tindakan (tidak diulang).\n" +
            "\n" +
            "## Situasi 6 — Butang 'Segar Semula' selepas check-in\n" +
            "• Di latar depan; aktiviti terakhir ialah check-in.\n" +
            "• Pengguna ketik 'Segar Semula'.\n" +
            "• Check-in baharu HANYA jika lokasi BERBEZA daripada check-in terakhir (peraturan sama seperti Situasi 4). Di tempat yang SAMA, TIADA tindakan.\n" +
            "\n" +
            "## Situasi 7 — Meninggalkan Zon Check-Out\n" +
            "• Di latar depan; aktiviti terakhir ialah check-out; pengguna di 'Zon Check-Out' (tiada tindakan).\n" +
            "• Pengguna ketik 'Segar Semula' dan aplikasi mengemas kini lokasi kepada: Varian 7A — kawasan berdaftar selain 'Zon Check-Out'; Varian 7B — tiada kawasan berdaftar, tetapi masih berhampiran (kurang 2 km, kecuali Zon Check-Out).\n" +
            "• 7A: memandangkan aktiviti terakhir ialah check-out, aplikasi segera check-in di kawasan yang sepadan.\n" +
            "• 7B: memandangkan pengguna dalam keadaan check-out dan TIDAK di dalam mana-mana kawasan, aplikasi TIDAK check-in; ia hanya memaparkan 'Lokasi Tidak Berdaftar' (peraturan sama seperti nota Situasi 3).\n" +
            "\n" +
            "## Situasi 8 — Zon Campuran\n" +
            "• Aplikasi mengesan 'Zon Campuran' dan, jika aktiviti relevan terakhir bukan di dalamnya, menukar segera: 8A — selepas check-in → check-out di 'Zon Campuran'; 8B — selepas check-out → check-in di 'Zon Campuran'.\n" +
            "• 'Selang Masa Zon Campuran' ialah masa rehat untuk bacaan berturutan dalam Zon Campuran sahaja: selagi masa_berlalu < selang, penukaran baharu disekat; apabila >= selang, ia dibenarkan semula.\n" +
            "• Pengecualian segera (membuang masa rehat): pergi ke 'Zon Check-Out' atau melepasi jarak minimum → check-out; pergi ke kawasan berdaftar lain atau kekal dalam jarak minimum → check-in.\n" +
            "\n" +
            "## Situasi 9 — Mod manual (Aktiviti Automatik dimatikan)\n" +
            "• Pengguna disahkan; Aktiviti Automatik DIMATIKAN.\n" +
            "• Aplikasi mengemas kini lokasi jika ada kebenaran; jika tidak, ia memaparkan 'Kebenaran ditolak'.\n" +
            "• Pengguna memilih check-in/check-out, Biasa/Retroaktif, memilih 'Lokasi', dan ketik 'Rekod'.\n" +
            "• Aplikasi mengikut aliran biasa mengikut pilihan.",
        "notesTitle" to "Nota umum",
        "notesBody" to "## Pencetus latar depan\n" +
            "• Membuka aplikasi atau membawanya ke latar depan, dengan Aktiviti Automatik dihidupkan dan pengguna disahkan, mencetuskan penilaian automatik (enjin memutuskan check-in atau check-out mengikut situasi). Perkara sama terpakai pada geofencing dan semakan berkala setiap 15 minit.\n" +
            "• Tiada check-in berkala 'membuta tuli': semakan 15 minit sentiasa mengesahkan lokasi dan mengekalkan peraturan 'langkau jika tiada perubahan'.\n" +
            "\n" +
            "## Check-in hanya pada perubahan lokasi\n" +
            "• Check-in automatik berlaku hanya apabila lokasi yang diselesaikan BERBEZA daripada check-in terakhir. Lokasi sama → tiada tindakan. Peraturan ini (Situasi 4 dan 6) yang MENGHAPUSKAN check-in berganda.\n" +
            "\n" +
            "## BORANG mengikut projek\n" +
            "• Pada check-in pertama hari itu dan pada setiap check-out, borang diisi dan dihantar SEKALI BAGI SETIAP PROJEK yang pengguna didaftarkan (menghormati 'borang dihidupkan' setiap projek). Cth.: pengguna dalam projek P80 dan P83 → dua penghantaran. Pengguna projek tunggal → satu penghantaran.\n" +
            "\n" +
            "## Invarian check-out (dikekalkan)\n" +
            "• Check-out automatik berlaku dalam semua kes yang diterangkan (Zon Check-Out, jarak melepasi had, penukaran Zon Campuran); tidak pernah ada dua check-out berturutan; selepas check-out, aktiviti automatik seterusnya sentiasa check-in.",
    ),
)

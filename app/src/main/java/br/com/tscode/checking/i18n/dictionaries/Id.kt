package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun idDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Manual Checking",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "Formulir pencatatan check-in dan check-out",
        "credentialsAria" to "Identifikasi pengguna dan kata sandi",
        "keyLabel" to "Kunci",
        "passwordLabel" to "Kata Sandi",
        "keyPlaceholder" to "Contoh: HR70",
        "passwordPlaceholder" to "3 hingga 10 karakter",
        "requestRegistrationButton" to "Minta pendaftaran",
        "awaitingApproval" to "Menunggu persetujuan pendaftaran.",
        "registrationQueueFull" to "Antrean pendaftaran penuh. Silakan hubungi administrator sistem.",
        "settingsSpacer" to "Pengaturan",
        "openSettingsAria" to "Buka pengaturan",
        "openSettingsTitle" to "Buka pengaturan",
        "waitingAuthentication" to "Menunggu autentikasi.",
        "enterPasswordPrompt" to "Masukkan kata sandi untuk memulai.",
        "createPasswordPrompt" to "Masukkan kunci Anda dan buat kata sandi.",
        "invalidFourCharacterKey" to "Masukkan kunci alfanumerik 4 karakter.",
        "unknownUserDetail" to "Kunci pengguna belum terdaftar",
        "transportAccessPrompt" to "Masukkan kunci Anda dan validasi kata sandi untuk mengakses Transportasi.",
    ),
    "history" to d(
        "lastCheckinLabel" to "Check-In Terakhir",
        "lastCheckoutLabel" to "Check-Out Terakhir",
        "today" to "Hari Ini",
        "yesterday" to "Kemarin",
        "dialogTitleCheckin" to "Riwayat Check-In",
        "dialogTitleCheckout" to "Riwayat Check-Out",
        "colDate" to "Tanggal",
        "colTime" to "Waktu",
        "colLocal" to "Lokasi",
        "empty" to "Tidak ada catatan.",
        "back" to "Kembali",
        "loadingMessage" to "Memeriksa riwayat...",
        "notFoundMessage" to "Tidak ada catatan untuk kunci ini.",
        "noRecordsMessage" to "Tidak ada check-in atau check-out yang tercatat untuk kunci ini.",
        "updatedMessage" to "Riwayat untuk kunci yang dimasukkan berhasil diperbarui.",
        "loadFailed" to "Tidak dapat memeriksa riwayat untuk kunci ini.",
        "colActivity" to "Aktivitas",
        "activityCheckin" to "Check-In",
        "activityCheckout" to "Check-Out",
        "loadError" to "Tidak dapat memuat riwayat.",
        "retry" to "Coba lagi",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "Aktivitas Otomatis",
        "sectionTitle" to "Pendaftaran",
        "checkinLabel" to "Check-In",
        "checkoutLabel" to "Check-Out",
        "transportLabel" to "Transportasi",
        "informeTitle" to "Jenis",
        "informeNormalLabel" to "Normal",
        "informeRetroativoLabel" to "Retroaktif",
        "submitButton" to "Daftar",
        "checkInLowerLabel" to "check-in",
        "checkOutLowerLabel" to "check-out",
        "disableAutomaticActivitiesForManualSubmit" to "Nonaktifkan Aktivitas Otomatis untuk melakukan pendaftaran manual.",
        "selectLocationBeforeSubmit" to "Pilih lokasi sebelum mendaftar.",
    ),
    "settings" to d(
        "title" to "Pengaturan",
        "languageLabel" to "Bahasa",
        "resetPasswordLabel" to "Ubah Kata Sandi",
        "allowLocationLabel" to "Izinkan Lokasi",
        "allowAudioVideoLabel" to "Izinkan Audio & Video",
        "supportLabel" to "Dukungan",
        "manualLabel" to "Petunjuk Penggunaan",
        "aboutLabel" to "Tentang",
        "activitiesLabel" to "Aktivitas",
        "backButton" to "Kembali",
    ),
    "passwordDialog" to d(
        "titleChange" to "Ubah Kata Sandi",
        "titleRegister" to "Buat Kata Sandi",
        "oldPasswordLabel" to "Kata Sandi Lama",
        "newPasswordLabel" to "Kata Sandi Baru",
        "confirmPasswordLabel" to "Konfirmasi Kata Sandi",
        "backButton" to "Kembali",
        "submitChangeButton" to "Ubah",
        "submitRegisterButton" to "Simpan",
        "changingStatus" to "Sedang mengubah kata sandi...",
        "savingStatus" to "Sedang menyimpan kata sandi...",
        "validatingStatus" to "Sedang memverifikasi kata sandi.",
        "oldPasswordInvalid" to "Kata sandi lama harus 3 sampai 10 karakter.",
        "newPasswordInvalid" to "Kata sandi baru harus 3 sampai 10 karakter.",
        "confirmMismatch" to "Konfirmasi kata sandi baru tidak cocok.",
        "changeFailed" to "Tidak dapat mengubah kata sandi.",
        "validationFailed" to "Tidak dapat memverifikasi kata sandi.",
        "statusLoadFailed" to "Tidak dapat memeriksa status kata sandi.",
    ),
    "registrationDialog" to d(
        "title" to "Minta Pendaftaran",
        "note" to "Isi informasi di bawah ini untuk menggunakan sistem Checking.",
        "keyLabel" to "Kunci",
        "fullNameLabel" to "Nama Lengkap",
        "projectsLabel" to "Proyek",
        "projectsHint" to "Pilih satu atau lebih proyek.",
        "emailLabel" to "Email",
        "emailPlaceholder" to "Opsional",
        "passwordLabel" to "Kata Sandi",
        "confirmPasswordLabel" to "Konfirmasi Kata Sandi",
        "backButton" to "Kembali",
        "submitButton" to "Kirim",
        "loadingProjects" to "Memuat proyek...",
        "noProjectsAvailable" to "Tidak ada proyek yang tersedia saat ini.",
        "fullNameRequired" to "Masukkan nama lengkap.",
        "emailInvalid" to "Masukkan email yang valid atau biarkan kolom kosong.",
        "passwordInvalid" to "Kata sandi harus 3 sampai 10 karakter.",
        "confirmMismatch" to "Konfirmasi kata sandi baru tidak cocok.",
        "submittingStatus" to "Mengirim permintaan pendaftaran...",
        "successStatus" to "Pendaftaran berhasil diselesaikan.",
        "submitFailed" to "Tidak dapat mengirim permintaan pendaftaran.",
    ),
    "location" to d(
        "title" to "Lokasi",
        "waitingLabel" to "Menunggu lokasi.",
        "refreshLabel" to "Segarkan lokasi",
        "refreshBusyLabel" to "Menyegarkan lokasi",
        "unavailableShort" to "Tidak Tersedia",
        "unavailableLabel" to "Lokasi tidak tersedia",
        "unavailableMessage" to "Tidak dapat memeriksa lokasi saat ini.",
        "noPermissionLabel" to "Tanpa Izin",
        "timeoutLabel" to "Waktu Habis",
        "timeoutMessage" to "Pencarian lokasi memakan waktu lebih lama dari yang diharapkan.",
        "detectingLabel" to "Mendeteksi...",
        "exactConfirmationBrowser" to "Menunggu konfirmasi lokasi tepat dari browser.",
        "exactConfirmationApp" to "Menunggu konfirmasi lokasi tepat dari pintasan/aplikasi.",
        "updatingDeviceLocation" to "Memperbarui lokasi perangkat saat ini.",
        "secureContextRequired" to "Lokasi presisi memerlukan koneksi aman (HTTPS).",
        "browserUnsupported" to "Browser ini tidak mendukung lokasi presisi.",
        "permissionBlocked" to "Izin lokasi diblokir di browser. Izinkan akses situs di pengaturan browser.",
        "captureRequiresSupport" to "Pengambilan lokasi memerlukan HTTPS dan dukungan browser.",
        "noValidPosition" to "Tidak dapat memperoleh posisi perangkat yang valid.",
        "searchingPrecision" to "Mencari tingkat presisi yang cukup...",
        "completionStatus" to "Pembaruan lokasi selesai.",
        "completionStatusWithDetail" to "Pembaruan lokasi selesai. {detail}",
        "browserContextLabel" to "di browser ini",
        "appContextLabel" to "di pintasan/aplikasi ini",
        "browserSourceLabel" to "melalui browser",
        "appSourceLabel" to "melalui pintasan/aplikasi",
        "currentAccuracyLabel" to "Akurasi saat ini",
        "accuracyPrefix" to "Akurasi",
        "accuracyTemplate" to "Akurasi {accuracy}",
        "accuracyLimitTemplate" to "Batas {limit} m",
        "accuracyCombinedTemplate" to "Akurasi {accuracy} / Batas {limit} m",
        "noKnownLocations" to "Tidak ada lokasi terdaftar",
        "defaultManualLocationLabel" to "Kantor Utama",
        "accuracyFallbackManualLocationLabel" to "Akurasi Tidak Cukup",
        "outsideWorkplaceLabel" to "Di Luar Tempat Kerja",
        "unregisteredLocationLabel" to "Lokasi Tidak Terdaftar",
        "mixedZoneLabel" to "Zona Campuran",
        "checkoutZoneLabel" to "Zona check-out",
    ),
    "projects" to d(
        "label" to "Proyek",
        "changeButton" to "Ubah",
        "loadingProjects" to "Memuat proyek...",
        "updatingProjects" to "Memperbarui proyek...",
        "noneAvailableShort" to "Tidak ada proyek tersedia",
        "noneAvailableSentence" to "Tidak ada proyek tersedia.",
        "noneAvailableNow" to "Tidak ada proyek yang tersedia saat ini.",
        "selectAtLeastOne" to "Pilih setidaknya satu proyek.",
        "userProjectsAria" to "Proyek pengguna",
        "registrationProjectsAria" to "Proyek pendaftaran",
        "updatedSuccess" to "Proyek berhasil diperbarui.",
        "loadFailed" to "Tidak dapat memuat proyek.",
        "userProjectsLoadFailed" to "Tidak dapat memuat proyek pengguna.",
        "updateFailed" to "Tidak dapat memperbarui proyek.",
    ),
    "transport" to d(
        "title" to "Penjadwalan Transportasi",
        "backToMainAria" to "Kembali ke layar utama",
        "addressToggleLabel" to "Alamat:",
        "addressLabel" to "Alamat:",
        "zipLabel" to "Kode ZIP:",
        "addressPlaceholder" to "Blok (jika ada), jalan, dan nomor.",
        "zipPlaceholder" to "Hanya 6 digit",
        "addressBackButton" to "Kembali",
        "addressSubmitButton" to "Simpan",
        "optionInstruction" to "Pilih jenis transportasi untuk melanjutkan.",
        "historyTitle" to "Permintaan aktif",
        "historyButtonLabel" to "Riwayat",
        "historyPanelTitle" to "Riwayat Permintaan",
        "historyCloseButton" to "Tutup",
        "kinds" to d(
            "regular" to "Hari Kerja",
            "weekend" to "Akhir Pekan",
            "extra" to "Tanggal Tertentu",
        ),
        "statusLabels" to d(
            "available" to "Tidak ada permintaan",
            "pending" to "Menunggu",
            "confirmed" to "Dikonfirmasi",
            "realized" to "Selesai",
            "rejected" to "Ditolak",
            "cancelled" to "Dibatalkan",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "Sen",
                "1" to "Sel",
                "2" to "Rab",
                "3" to "Kam",
                "4" to "Jum",
                "5" to "Sab",
                "6" to "Min",
            ),
            "full" to d(
                "0" to "Senin",
                "1" to "Selasa",
                "2" to "Rabu",
                "3" to "Kamis",
                "4" to "Jumat",
                "5" to "Sabtu",
                "6" to "Minggu",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "Pilih hari:",
            "regularSubtitle" to "Pilih hari kerja yang diinginkan untuk permintaan ini.",
            "weekendSubtitle" to "Pilih hari akhir pekan yang diinginkan untuk permintaan ini.",
            "extraSubtitle" to "Periksa tanggal dan waktu sebelum meminta.",
            "dateLabel" to "Tanggal:",
            "timeLabel" to "Waktu:",
            "backButton" to "Kembali",
            "submitButton" to "Minta",
            "requestUnavailable" to "Permintaan transportasi tidak tersedia.",
            "addressRequired" to "Daftarkan alamat lengkap sebelum meminta transportasi.",
            "dateRequiredExtra" to "Masukkan tanggal untuk transportasi tambahan.",
            "timeRequiredExtra" to "Masukkan waktu untuk transportasi tambahan.",
            "dayRequired" to "Pilih setidaknya satu hari untuk meminta transportasi.",
            "conflictGeneric" to "Sudah ada permintaan transportasi aktif untuk tanggal tersebut.",
            "conflictByDate" to "Sudah ada permintaan transportasi aktif untuk {serviceDateLabel}.",
        ),
        "summary" to d(
            "noRequestRecorded" to "Belum ada permintaan tercatat.",
            "noActiveRequests" to "Tidak ada permintaan aktif.",
            "noRequestStatus" to "Tidak ada permintaan",
            "waitingAllocation" to "Menunggu alokasi transportasi.",
            "vehicleAllocated" to "Kendaraan sudah dialokasikan.",
            "scheduleUnavailable" to "Jadwal tidak tersedia.",
            "requestClosed" to "Permintaan ditutup.",
            "whenRequestExists" to "Saat ada permintaan, informasinya akan muncul di sini.",
            "whenAllocated" to "Saat Anda dialokasikan ke kendaraan, informasinya akan muncul di sini.",
            "departureAndLimit" to "Berangkat {departureTime} • Batas {deadlineTime}",
            "limitOnly" to "Batas {deadlineTime}",
        ),
        "detail" to d(
            "title" to "Detail Permintaan",
            "genericTitle" to "Transportasi",
            "waitingAllocation" to "Menunggu alokasi transportasi.",
            "whenAllocated" to "Saat Anda dialokasikan ke kendaraan, informasinya akan muncul di sini.",
            "inactive" to "Permintaan ini tidak lagi aktif.",
            "confirmed" to "Transportasi dikonfirmasi.",
            "realized" to "Transportasi selesai.",
            "vehicleTypeLabel" to "Jenis Kendaraan",
            "vehiclePlateLabel" to "Pelat Kendaraan",
            "vehicleColorLabel" to "Warna Kendaraan",
            "departureDateLabel" to "Tanggal Keberangkatan",
            "departureTimeLabel" to "Waktu Keberangkatan",
            "unavailableValue" to "Tidak tersedia",
        ),
        "actions" to d(
            "markRealized" to "Selesai",
            "cancel" to "Batal",
            "cancelling" to "Membatalkan...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "Masukkan kunci yang valid sebelum memperbarui alamat.",
            "invalidKeyBeforeRequest" to "Masukkan kunci yang valid sebelum meminta transportasi.",
            "requestFailed" to "Tidak dapat meminta {requestLabel}.",
            "loadFailed" to "Tidak dapat memeriksa transportasi.",
            "addressUpdated" to "Alamat berhasil diperbarui.",
            "addressUpdateFailed" to "Tidak dapat memperbarui alamat.",
            "cancelSuccess" to "Permintaan transportasi dibatalkan.",
            "cancelFailed" to "Tidak dapat membatalkan permintaan.",
            "requestMarkedRealized" to "Permintaan ditandai selesai.",
            "accessRequiresAuthentication" to "Masukkan kunci Anda dan validasi kata sandi untuk mengakses Transportasi.",
        ),
    ),
    "status" to d(
        "validationError" to "Kesalahan validasi.",
        "apiCommunicationFailure" to "Komunikasi API gagal.",
        "passwordVerifying" to "Sedang memverifikasi kata sandi.",
        "authenticationCompleted" to "Autentikasi selesai. Memperbarui aplikasi...",
        "userAuthenticated" to "Pengguna diautentikasi. Memulai pembaruan.",
        "applicationUpdated" to "Aplikasi berhasil diperbarui.",
        "applicationUpdateFailed" to "Tidak dapat memperbarui aplikasi sekarang.",
        "checkinCompleted" to "Check-In selesai.",
        "checkoutCompleted" to "Check-Out selesai.",
        "savedOffline" to "Disimpan secara luring. Akan disinkronkan saat koneksi kembali.",
        "automaticCheckinCompleted" to "Check-In otomatis selesai.",
        "automaticCheckoutCompleted" to "Check-Out otomatis selesai.",
        "updatingActivitiesSequence" to "Memperbarui aktivitas.....",
        "updatingLocationSequence" to "Memperbarui lokasi.....",
        "runningAutomaticActivitySequence" to "Menjalankan check-in atau check-out bila diperlukan.....",
        "automaticUpdatesRunning" to "Pembaruan sedang berlangsung.",
        "automaticUpdatesCompletedWithActivity" to "Pembaruan selesai dengan {activity} dilakukan.",
        "automaticUpdatesCompletedWithoutActivity" to "Pembaruan selesai tanpa aktivitas yang dilakukan.",
        "automaticUpdatesFailed" to "Tidak dapat menyelesaikan pembaruan otomatis sekarang.",
        "automaticActivitiesDisabled" to "Mode 100% manual telah diaktifkan.",
        "operationFailed" to "Tidak dapat menyelesaikan operasi.",
    ),
    "manual" to d(
        "eyebrow" to "Checking Web • Panduan penggunaan",
        "heading" to "Petunjuk Penggunaan",
        "introPrimary" to "Manual ini merangkum alur utama autentikasi, pencatatan kehadiran, lokasi, transportasi, dan dukungan pada Checking Web.",
        "introSecondary" to "Gunakan halaman ini sebagai rujukan cepat untuk memahami apa yang dijalankan aplikasi secara otomatis dan tindakan mana yang tetap berada dalam kendali pengguna.",
        "currentLanguageLabel" to "Bahasa halaman",
        "availabilityNote" to "Pada tahap ini, manual lengkap tersedia dalam bahasa Portugis dan Inggris.",
        "highlights" to d(
            "accessTitle" to "Akses terpandu",
            "accessBody" to "Aplikasi memutuskan secara otomatis kapan harus meminta pendaftaran pengguna, pembuatan kata sandi, atau autentikasi biasa.",
            "locationTitle" to "Lokasi terpantau",
            "locationBody" to "Izin, akurasi GPS, dan pencatatan manual cadangan memengaruhi langsung alur pencatatan kehadiran.",
            "supportTitle" to "Bantuan cepat",
            "supportBody" to "Pengaturan memusatkan bahasa, kata sandi, lokasi, dukungan melalui WhatsApp, dan akses ke dokumentasi ini.",
        ),
        "tocTitle" to "Peta manual",
        "tocAriaLabel" to "Navigasi manual",
        "snapshotSlotLabel" to "Slot tangkapan layar",
        "toc" to d(
            "overview" to "Gambaran umum",
            "authFlow" to "Alur autentikasi",
            "userRegistration" to "Pendaftaran pengguna otomatis",
            "passwordRegistration" to "Pendaftaran kata sandi otomatis",
            "login" to "Masuk",
            "attendance" to "Check-in dan check-out",
            "projectSelection" to "Pemilihan proyek",
            "location" to "Izin lokasi",
            "automaticActivities" to "Aktivitas otomatis",
            "transport" to "Transportasi",
            "passwordChange" to "Atur ulang/ubah kata sandi",
            "settings" to "Pengaturan",
            "support" to "Dukungan",
            "faq" to "Masalah umum dan FAQ",
        ),
        "sections" to d(
            "overview" to d(
                "title" to "Gambaran umum",
                "lead" to "Checking Web menggabungkan autentikasi, pencatatan kehadiran, konteks lokasi, dan akses transportasi dalam satu antarmuka yang dioptimalkan untuk ponsel.",
                "item1" to "Layar utama menampilkan kunci, kata sandi, riwayat terbaru, formulir pencatatan kehadiran, dan pintasan ke Transportasi.",
                "item2" to "Alur bantuan muncul secara otomatis saat kunci belum ada atau saat pengguna masih perlu membuat kata sandi pertama.",
                "item3" to "Menu Pengaturan mengelompokkan tindakan sekunder agar area utama tetap ringkas.",
                "figureCaption" to "Layar utama Checking Web dengan area autentikasi dan formulir pencatatan kehadiran.",
            ),
            "authFlow" to d(
                "title" to "Alur autentikasi",
                "lead" to "Aplikasi memeriksa status kunci begitu kunci mencapai empat karakter yang valid dan menentukan alur bantuan mana yang ditampilkan.",
                "item1" to "Jika kunci tidak ada, alur beralih ke pendaftaran pengguna tanpa menunggu klik tambahan.",
                "item2" to "Jika kunci ada tetapi belum memiliki kata sandi, antarmuka langsung membuka pendaftaran kata sandi.",
                "item3" to "Jika kunci dan kata sandi sudah ada, antarmuka tetap berada pada jalur masuk normal.",
                "note" to "Menutup dialog secara manual tidak menimbulkan perulangan tanpa akhir: sistem hanya mencoba lagi saat kunci atau status autentikasi terkait benar-benar berubah.",
            ),
            "userRegistration" to d(
                "title" to "Pendaftaran pengguna otomatis",
                "lead" to "Saat kunci tidak ada di basis data, aplikasi membuka formulir layanan mandiri agar pengguna baru dapat menyelesaikan pendaftaran.",
                "item1" to "Formulir meminta kunci, nama lengkap, proyek, email opsional, kata sandi, dan konfirmasi kata sandi.",
                "item2" to "Proyek dimuat dari API dan pengguna dapat menandai satu atau beberapa item yang valid.",
                "item3" to "Setelah pengiriman berhasil, sesi web terautentikasi dan aplikasi tetap terbuka untuk digunakan.",
                "figureCaption" to "Dialog pendaftaran pengguna baru yang terbuka otomatis untuk kunci yang belum dikenal.",
            ),
            "passwordRegistration" to d(
                "title" to "Pendaftaran kata sandi otomatis",
                "lead" to "Jika kunci sudah ada tetapi belum ada kata sandi terdaftar, Checking Web masuk ke mode pembuatan kata sandi pertama.",
                "item1" to "Dialog menggunakan kembali komponen kata sandi yang ada, tetapi menyembunyikan kolom kata sandi lama.",
                "item2" to "Pengguna memasukkan kata sandi baru, mengonfirmasi nilainya, dan menyelesaikan akses pertama.",
                "item3" to "Setelah disimpan, backend mengautentikasi sesi web dan layar utama menerima operasi selebihnya.",
                "figureCaption" to "Dialog pembuatan kata sandi pertama untuk pengguna yang sudah dikenal sistem.",
            ),
            "login" to d(
                "title" to "Masuk",
                "lead" to "Pengguna yang sudah terdaftar mengetik kunci, memasukkan kata sandi, dan membiarkan aplikasi memvalidasi autentikasi sebelum mencatat kehadiran atau membuka Transportasi.",
                "item1" to "Kata sandi dapat diverifikasi secara otomatis oleh alur yang sudah ada pada antarmuka utama.",
                "item2" to "Jika pengguna mengubah kata sandi yang diketik setelah terautentikasi, aplikasi kembali melindungi diri hingga nilai baru diverifikasi lagi.",
                "item3" to "Pesan status pada antarmuka membantu membedakan menunggu kata sandi, sedang memverifikasi, dan terautentikasi.",
            ),
            "attendance" to d(
                "title" to "Check-in dan check-out",
                "lead" to "Dengan autentikasi yang valid, pengguna memilih tipe pencatatan, meninjau konteks, dan mengirim operasi langsung dari layar utama.",
                "item1" to "Formulir membedakan check-in dari check-out dan dapat memblokir pengiriman manual saat aktivitas otomatis sedang mengendalikan alur.",
                "item2" to "Riwayat yang terlihat di bagian atas membantu memastikan peristiwa terakhir yang tercatat sebelum mengirim peristiwa baru.",
                "item3" to "Pesan keberhasilan atau kegagalan muncul di area status setelah setiap percobaan.",
                "figureCaption" to "Contoh keadaan berhasil setelah sebuah pencatatan kehadiran.",
            ),
            "projectSelection" to d(
                "title" to "Pemilihan proyek",
                "lead" to "Aplikasi menggunakan proyek baik pada pendaftaran awal maupun pada rutinitas harian, untuk membatasi konteks pengguna dan lokasi yang tersedia.",
                "item1" to "Pada pendaftaran, pengguna menandai satu atau beberapa proyek yang valid sebelum menyelesaikan pembuatan akun.",
                "item2" to "Setelah terautentikasi, panel utama menampilkan proyek saat ini dan memungkinkan pembaruan ketika kemampuan itu tersedia.",
                "item3" to "Proyek aktif memengaruhi daftar lokasi, riwayat kontekstual, dan area lain yang bergantung pada cakupan pengguna.",
                "figureCaption" to "Contoh blok proyek dalam konteks terautentikasi, dengan ringkasan cakupan aktif.",
            ),
            "location" to d(
                "title" to "Izin lokasi dan perilaku GPS",
                "lead" to "Posisi perangkat digunakan untuk menentukan konteks operasional, memicu alur otomatis, dan mengarahkan pengguna saat akurasi belum cukup.",
                "item1" to "Aplikasi bergantung pada HTTPS, dukungan peramban, dan izin aktif untuk mengakses lokasi presisi.",
                "item2" to "Saat izin ditolak atau tidak tersedia, aplikasi menampilkan pesan yang jelas dan dapat membatasi alur pada pencatatan manual cadangan yang diizinkan.",
                "item3" to "Saat lokasi berhasil diperoleh, antarmuka memperbarui akurasi, tempat yang dikenali, dan tindakan otomatis terkait.",
                "figureCaptionDenied" to "Contoh layar saat izin lokasi belum tersedia.",
                "figureCaptionGranted" to "Contoh layar setelah akses lokasi presisi berhasil diberikan.",
            ),
            "automaticActivities" to d(
                "title" to "Aktivitas otomatis",
                "lead" to "Aplikasi juga dapat memicu check-in, check-out, dan pembaruan terkait saat konteks lokasi berubah sesuai harapan.",
                "item1" to "Mekanisme ini bergantung pada autentikasi yang valid, pembacaan lokasi, dan aturan internal yang menghindari peralihan yang tidak aman.",
                "item2" to "Saat aktivitas otomatis mengendalikan alur, sebagian kolom manual menjadi tersembunyi atau dinonaktifkan untuk menjaga konsistensi catatan.",
                "item3" to "Jika akurasi turun di bawah ambang yang diperlukan, aplikasi dapat membuka kembali opsi pencatatan manual cadangan alih-alih mengambil keputusan berisiko.",
            ),
            "transport" to d(
                "title" to "Akses Transportasi",
                "lead" to "Setelah autentikasi, pengguna dapat membuka modul Transportasi untuk meminta perjalanan, memeriksa status, dan meninjau detail permintaan terbaru.",
                "item1" to "Akses tetap terlindungi oleh kunci dan kata sandi yang sama yang divalidasi pada antarmuka utama.",
                "item2" to "Modul ini mencakup pendaftaran alamat, permintaan per tipe transportasi, dan ringkasan status saat ini.",
                "item3" to "Saat ada kendaraan yang dialokasikan, layar menampilkan detail operasional utama bagi pengguna.",
                "figureCaption" to "Contoh antarmuka Transportasi yang diakses dari antarmuka Checking Web.",
            ),
            "passwordChange" to d(
                "title" to "Atur ulang atau ubah kata sandi",
                "lead" to "Penggantian kata sandi tidak lagi berada pada baris autentikasi utama dan kini terpusat di Pengaturan.",
                "item1" to "Buka Pengaturan, ketuk Ubah Kata Sandi, dan gunakan dialog penggantian kata sandi yang sudah ada.",
                "item2" to "Alur meminta kata sandi lama, kata sandi baru, dan konfirmasi, dengan tetap mempertahankan validasi yang ada.",
                "item3" to "Tombol hanya aktif saat pengguna sudah terautentikasi dan memiliki kata sandi terdaftar.",
                "figureCaption" to "Alur penggantian kata sandi yang dimulai dari Pengaturan > Ubah Kata Sandi.",
            ),
            "settings" to d(
                "title" to "Pengaturan",
                "lead" to "Ikon roda gigi membuka titik pusat untuk preferensi dan tindakan sekunder, tanpa memenuhi baris autentikasi.",
                "item1" to "Bahasa mengubah teks yang terlihat pada aplikasi utama dan menyimpan preferensi di peramban.",
                "item2" to "Izinkan Lokasi menggunakan kembali alur yang ada untuk meminta akses presisi sekali lagi saat hal itu masih diperlukan.",
                "item3" to "Dukungan dan Tentang menggunakan titik masuk yang sama untuk membuka WhatsApp dan dokumentasi ini.",
                "figureCaption" to "Widget Pengaturan dengan tindakan sekunder baru yang terpusat.",
            ),
            "support" to d(
                "title" to "Dukungan",
                "lead" to "Saat pengguna butuh bantuan manusia, Pengaturan > Dukungan menyiapkan percakapan WhatsApp dengan kunci pengguna yang sudah disertakan pada pesan pertama.",
                "item1" to "Tautan menggunakan nomor telepon resmi yang dikonfigurasi pada frontend Checking Web.",
                "item2" to "Pesan awal disiapkan secara otomatis untuk mengurangi hambatan dan mempercepat layanan.",
                "item3" to "Jika tidak ada kunci valid yang tersedia, tombol dukungan tetap dinonaktifkan demi keamanan.",
            ),
            "faq" to d(
                "title" to "Masalah umum dan FAQ",
                "lead" to "Gunakan jawaban cepat ini saat antarmuka terasa macet atau saat alur tidak berlanjut seperti yang diharapkan.",
                "q1" to "Mengapa aplikasi membuka alur pendaftaran dengan sendirinya?",
                "a1" to "Ini terjadi saat kunci tidak ada atau saat akun masih belum memiliki kata sandi. Alur baru menghilangkan klik tambahan dan membawa pengguna langsung ke tindakan yang tepat.",
                "q2" to "Mengapa tombol Izinkan Lokasi dinonaktifkan?",
                "a2" to "Karena aplikasi sudah memahami bahwa izin presisi aktif, atau karena pembaruan lokasi sedang berjalan.",
                "q3" to "Apa yang harus dilakukan jika Transportasi tidak terbuka?",
                "a3" to "Pertama, pastikan kunci dan kata sandi telah terautentikasi. Jika masalah berlanjut, buka Dukungan agar kunci Anda dapat dikirim ke tim layanan.",
            ),
            "scheduledPause" to d(
                "title" to "Jeda Terjadwal",
                "lead" to "Jeda Terjadwal menghemat baterai dengan menjeda aktivitas otomatis selama suatu periode (misalnya pada malam hari).",
                "item1" to "Di Pengaturan, ketuk Jeda Terjadwal.",
                "item2" to "Aktifkan opsi tersebut dan tetapkan waktu Dari dan Sampai (misalnya 22:00 hingga 06:00).",
                "item3" to "Jika mau, centang juga Jeda pada Sabtu dan/atau Jeda pada Minggu. Selama jeda, aplikasi tidak melakukan aktivitas otomatis apa pun dan melanjutkan sendiri di akhir periode.",
            ),
            "accident" to d(
                "title" to "Jika terjadi kecelakaan",
                "lead" to "Mode Kecelakaan adalah fitur keselamatan. Gunakan hanya dalam keadaan darurat nyata.",
                "item1" to "Setiap pengguna dapat membuka Mode Kecelakaan; ini memberi tahu, secara waktu nyata, semua pengguna pada proyek yang sama.",
                "item2" to "Laporkan kondisi dan zona Anda: aman, di lokasi kecelakaan tetapi aman, atau di lokasi kecelakaan dan butuh bantuan. Jika memungkinkan, rekam video lokasi: video dikirim secara waktu nyata ke panel administrator.",
                "item3" to "Tombol Hubungi Layanan Darurat menelepon layanan darurat setempat, melaporkan kecelakaan dan lokasi dalam bahasa wilayah tersebut.",
            ),
        ),
        "figures" to d(
            "authShellAlt" to "Layar utama Checking Web dengan kolom kunci dan kata sandi.",
            "userRegistrationAlt" to "Formulir pendaftaran pengguna baru di Checking Web.",
            "passwordRegistrationAlt" to "Dialog pendaftaran kata sandi awal untuk pengguna yang sudah ada tanpa kata sandi.",
            "settingsModalAlt" to "Widget Pengaturan Checking Web terbuka dengan tindakan bahasa, kata sandi, lokasi, dukungan, dan tentang.",
            "passwordChangeAlt" to "Dialog penggantian kata sandi yang dibuka dari Pengaturan.",
            "locationDeniedAlt" to "Keadaan Checking Web dengan izin lokasi ditolak atau tidak tersedia.",
            "locationGrantedAlt" to "Keadaan Checking Web dengan lokasi presisi tersedia dan dibagikan.",
            "projectSelectionAlt" to "Area pemilihan proyek di Checking Web.",
            "transportScreenAlt" to "Layar modul Transportasi di dalam ekosistem Checking Web.",
            "checkSuccessAlt" to "Keadaan berhasil setelah check-in atau check-out di Checking Web.",
        ),
    ),
    "accident" to d(
        "button" to d(
            "report" to "Laporkan Kecelakaan",
            "reported" to "Kecelakaan Dilaporkan",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "Saya membutuhkan bantuan dengan aplikasi Web. Kunci saya adalah {chave}.",
    ),
    "instructions" to d(
        "heading" to "Petunjuk",
        "intro" to "Panduan ini menunjukkan, langkah demi langkah, cara menggunakan Checking: mencatat kehadiran secara manual, mengaktifkan Mode Otomatis (check-in dan check-out berdasarkan lokasi) dan mengatur Jeda Terjadwal.",
        "step1" to d(
            "title" to "1. Masuk ke aplikasi",
            "item1" to "Di layar utama, masukkan kunci 4 karakter Anda pada kolom 'Kunci'. Kolom berubah jingga saat kunci ditemukan.",
            "item2" to "Masukkan kata sandi Anda pada kolom 'Kata Sandi'. Setelah terverifikasi, kolom berubah hijau dan muncul 'Verifikasi selesai'.",
            "item3" to "Jika Anda belum punya kata sandi, aplikasi membuka pembuatan kata sandi secara otomatis; jika kunci tidak ada, ia menawarkan pendaftaran mandiri.",
        ),
        "step2" to d(
            "title" to "2. Catat kehadiran secara manual",
            "item1" to "Pilih 'Check-In' atau 'Check-Out' dan tipe 'Normal' atau 'Retroaktif'.",
            "item2" to "Dengan Mode Otomatis nonaktif, pilih 'Lokasi' dari daftar dan ketuk 'Catat Check-In' (atau 'Check-Out').",
            "item3" to "Kartu di bagian atas menampilkan check-in dan check-out terakhir Anda; ketuk untuk melihat daftar lengkap dengan tanggal, waktu, dan lokasi.",
        ),
        "step3" to d(
            "title" to "3. Aktifkan Mode Otomatis",
            "lead" to "Dengan Mode Otomatis, aplikasi melakukan check-in dan check-out sendiri berdasarkan lokasi Anda — saat memasuki atau meninggalkan area terdaftar, saat membawa aplikasi ke latar depan, dan pada pemeriksaan berkala.",
            "item1" to "Ketuk ikon roda gigi (di samping kolom kunci/kata sandi) untuk membuka 'Pengaturan'.",
            "item2" to "Ketuk 'Aktivitas Otomatis' dan centang kotak 'Aktifkan Aktivitas Otomatis'.",
            "item3" to "Berikan setiap izin pada daftar yang muncul dengan mengetuknya: Notifikasi, Lokasi 'Selalu izinkan', penggunaan Baterai tanpa batas, dan — pada sebagian perangkat — 'Mulai bersama perangkat'.",
            "item4" to "Saat roda gigi bercahaya HIJAU, Mode Otomatis aktif dan sehat. Cahaya JINGGA menandakan satu izin yang disarankan masih kurang.",
            "callout" to "Penting: agar berjalan andal di latar belakang, berikan Lokasi sebagai 'Selalu izinkan' dan matikan optimasi baterai untuk Checking.",
        ),
        "step4" to d(
            "title" to "4. Aktifkan Jeda Terjadwal",
            "lead" to "Jeda Terjadwal menghemat baterai dengan menjeda aktivitas otomatis selama suatu periode (misalnya pada malam hari).",
            "item1" to "Di 'Pengaturan', ketuk 'Jeda Terjadwal'.",
            "item2" to "Aktifkan opsi tersebut dan tetapkan waktu 'Dari' dan 'Sampai' (misalnya 22:00 hingga 06:00).",
            "item3" to "Jika mau, centang juga 'Jeda pada Sabtu' dan/atau 'Jeda pada Minggu'.",
            "item4" to "Selama jeda, aplikasi tidak melakukan aktivitas otomatis apa pun; ia melanjutkan sendiri di akhir periode.",
        ),
        "step5" to d(
            "title" to "5. Pantau riwayat",
            "item1" to "Ketuk 'CHECK-IN TERAKHIR' atau 'CHECK-OUT TERAKHIR' untuk membuka tabel dengan Tanggal, Waktu, dan Lokasi setiap catatan.",
            "item2" to "Catatan yang dibuat di dekat tetapi di luar area terdaftar muncul sebagai 'Lokasi Tidak Terdaftar'.",
            "item3" to "Bahkan tanpa internet, catatan Anda tersimpan di perangkat dan dikirim begitu koneksi kembali, selalu dengan waktu asli.",
        ),
        "step6" to d(
            "title" to "6. Minta transportasi",
            "item1" to "Ketuk 'Transportasi' untuk membuka modul transportasi personel.",
            "item2" to "Masukkan alamat dan waktu yang dibutuhkan lalu kirim permintaan.",
            "item3" to "Penanggung jawab logistik menyusun perjalanan; mesin kecerdasan buatan menyarankan cara mengelompokkan penumpang dan mengurutkan perhentian.",
        ),
        "step7" to d(
            "title" to "7. Jika terjadi kecelakaan",
            "lead" to "Mode Kecelakaan adalah fitur keselamatan. Gunakan hanya dalam keadaan darurat nyata.",
            "item1" to "Setiap pengguna dapat membuka Mode Kecelakaan; ini memberi tahu, secara waktu nyata, semua pengguna pada proyek yang sama.",
            "item2" to "Laporkan kondisi dan zona Anda: 'aman', 'di lokasi kecelakaan tetapi aman', atau 'di lokasi kecelakaan dan butuh bantuan'.",
            "item3" to "Jika memungkinkan, rekam video lokasi: video dikirim secara waktu nyata ke panel administrator.",
            "item4" to "Tombol 'Hubungi Layanan Darurat' menelepon layanan darurat setempat, melaporkan kecelakaan dan lokasi dalam bahasa wilayah tersebut.",
        ),
        "step8" to d(
            "title" to "8. Pengaturan lain",
            "item1" to "'Peringatan': pilih notifikasi yang Anda terima (aktivitas, jeda terjadwal, kecelakaan).",
            "item2" to "'Bahasa': ubah bahasa aplikasi.",
            "item3" to "'Ubah Kata Sandi': tetapkan kata sandi baru saat diperlukan.",
            "item4" to "'Dukungan': bicara langsung dengan tim melalui WhatsApp.",
            "item5" to "'Tentang': pelajari sejarah Checking dan bagian-bagian yang menyusun sistem.",
        ),
        "closing" to "Selesai! Dengan Mode Otomatis aktif, Anda tidak perlu mencatat apa pun secara manual — Checking mengurusnya untuk Anda.",
    ),
    "about" to d(
        "heading" to "Tentang Checking",
        "introTitle" to "Bagaimana Checking bermula",
        "introBody" to "Checking mulai dikembangkan pada Maret 2026, dari gagasan Insinyur Dilnei Schmidt.\n" +
            "\n" +
            "Ada kebutuhan untuk dengan cepat mengidentifikasi setiap karyawan Petrobras yang berada di lokasi kerja konstruksi dan pemasangan, seandainya terjadi kecelakaan.\n" +
            "\n" +
            "Solusi pertama dari manajemen SMS adalah formulir daring, diisi oleh setiap karyawan saat tiba dan meninggalkan lokasi kerja. Ini berhasil mengidentifikasi siapa yang hadir, tetapi merepotkan dan banyak yang kadang lupa mengisinya.\n" +
            "\n" +
            "Untuk meningkatkan efisiensi, Dilnei membuat aplikasi yang mampu:\n" +
            "• mengidentifikasi, melalui GPS, kedekatan pengguna dengan lokasi kerja dan mengingatkannya untuk check-in;\n" +
            "• menyetel alarm pada jam check-in dan check-out yang lazim, mengingatkan pengguna mengisi formulir;\n" +
            "• mengisi formulir secara otomatis dengan data pengguna dan mengirimnya daring.\n" +
            "\n" +
            "Ini memudahkan pekerjaan dan meningkatkan frekuensi pengisian formulir.\n" +
            "\n" +
            "Masih pada Maret 2026, Insinyur Tamer Salmem mengetahui solusi yang telah diterapkan dan memajukan penggunaan teknologi pemrograman terkini, mengembangkan sistem yang awalnya digagas oleh Dilnei.\n" +
            "\n" +
            "Tujuannya agar pengguna tidak perlu repot membuka aplikasi untuk check-in atau check-out. Selain itu, membangun pemantauan waktu nyata sehingga administrator tahu bukan hanya siapa yang sedang bekerja, tetapi di lokasi terdaftar mana dari setiap proyek setiap pengguna berada — meningkatkan kemampuan merespons dalam keadaan darurat.\n" +
            "\n" +
            "Maka, sistem memperoleh:\n" +
            "• pengaktifan layanan melalui geofencing (berdasarkan kedekatan pengguna dengan lokasi kerja);\n" +
            "• eksekusi tugas di latar belakang — check-in pada setiap perubahan lokasi di dalam fasilitas dan check-out saat pengguna menjauh, tanpa pengguna perlu membuka kunci perangkat;\n" +
            "• pengiriman lokasi pengguna secara waktu nyata ke panel administrator;\n" +
            "• kemampuan mendaftarkan sebanyak apa pun proyek yang diperlukan, di mana saja di dunia.\n" +
            "\n" +
            "Sistem juga dapat memasuki 'Mode Kecelakaan'. Jika terjadi kecelakaan, setiap pengguna dapat memicu alarm yang memberi tahu, secara waktu nyata, semua pengguna pada proyek yang sama. Dengan Mode Kecelakaan aktif:\n" +
            "• sebuah tabel dibuat pada panel administrator, mencantumkan kondisi setiap pengguna: 'aman', 'di lokasi kecelakaan tetapi aman', dan 'di lokasi kecelakaan dan butuh bantuan';\n" +
            "• pengguna dapat merekam video dan mengirimnya secara waktu nyata, sebagai tautan dalam tabel, agar administrator melihat rekaman lokasi;\n" +
            "• tombol 'Hubungi Layanan Darurat' menelepon layanan darurat setempat yang terdaftar, melaporkan kecelakaan, lokasi, dan narahubung, berbicara dalam bahasa wilayah tersebut.\n" +
            "\n" +
            "Ketangguhan dan keandalan sistem membawa keselamatan operasional dan respons segera bagi tim SMS Petrobras.\n" +
            "\n" +
            "Akhirnya, Insinyur Thiago Soares do Nascimento mengintegrasikan informasi yang dihasilkan sistem dengan dasbor manajemen yang ada, sehingga sistem baru bekerja berdampingan dengan pengisian formulir lama, menjaga kontrol manajemen tetap terbarui.\n" +
            "\n" +
            "Begitulah CHECKING lahir.",
        "partsTitle" to "Bagian-bagian sistem",
        "partsIntro" to "Checking adalah sistem kontrol kehadiran yang mencatat masuk dan keluarnya karyawan di lokasi kerja. Ia bekerja melalui berbagai saluran — pembaca kartu RFID yang dipasang di lokasi, aplikasi Android, halaman web yang dapat diakses dari ponsel, dan panel administrasi — menyatukan semuanya di satu tempat.\n" +
            "\n" +
            "Ia terdiri dari:\n" +
            "• sebuah API, dibuat dengan Python/FastAPI;\n" +
            "• sebuah situs web untuk administrator sistem;\n" +
            "• sebuah aplikasi Web, responsif untuk ponsel dan desktop;\n" +
            "• sebuah dasbor untuk transportasi personel;\n" +
            "• sebuah aplikasi khusus Android, dibuat dengan Kotlin.",
        "partApiTitle" to "API",
        "partApiBody" to "API adalah otak sistem. Setiap kali seseorang check-in atau check-out — melalui pembaca fisik, aplikasi, atau halaman web — ia menerima informasi tersebut, memeriksa kebenarannya, menyimpannya ke basis data, dan memberi tahu komponen lain secara waktu nyata.\n" +
            "\n" +
            "Ia juga mengisi formulir korporat Microsoft Forms secara otomatis setelah setiap catatan, mengoordinasikan sistem transportasi, memicu peringatan darurat jika terjadi kecelakaan, dan memastikan tidak ada data yang hilang saat koneksi tidak stabil.",
        "partWebsiteTitle" to "Situs web",
        "partWebsiteBody" to "Situs web adalah panel kontrol administrator. Melaluinya, Anda dapat melihat secara waktu nyata siapa yang check-in dan siapa yang check-out, serta mengelola setiap aspek sistem tanpa pengetahuan teknis.\n" +
            "\n" +
            "Fungsi utama: mendaftar dan menyunting karyawan, membuat proyek dan aturannya, menentukan area geografis yang dikenali sistem, melihat laporan kehadiran, dan mengekspor data. Ia juga titik pusat untuk memicu dan memantau Mode Kecelakaan — melihat kondisi setiap karyawan secara waktu nyata dan mengoordinasikan respons darurat.",
        "partWebappTitle" to "Aplikasi Web",
        "partWebappBody" to "Aplikasi web adalah alat para karyawan. Ia berjalan di peramban ponsel atau komputer, tanpa perlu memasang apa pun, dan memungkinkan setiap orang mencatat masuk atau keluar, melihat riwayat mereka, dan meminta transportasi.\n" +
            "\n" +
            "Saat karyawan mengaktifkan Aktivitas Otomatis, ponsel itu sendiri mendeteksi lokasi dan check-in atau check-out secara otomatis saat memasuki atau meninggalkan area terdaftar. Jika terjadi kecelakaan, antarmuka berubah dan meminta karyawan melaporkan kondisi dan zona keselamatannya.\n" +
            "\n" +
            "Ia tersedia dalam enam bahasa (Portugis, Inggris, Tionghoa, Melayu, Indonesia, dan Tagalog) untuk melayani tim internasional.",
        "partTransportTitle" to "Dasbor transportasi",
        "partTransportBody" to "Dasbor transportasi adalah alat bagi penanggung jawab logistik perjalanan. Melaluinya, Anda dapat mendaftarkan kendaraan, melihat dan menyusun permintaan transportasi yang dibuat karyawan, dan menugaskan setiap orang ke sebuah kendaraan untuk hari itu.\n" +
            "\n" +
            "Ia dilengkapi mesin kecerdasan buatan yang menganalisis alamat dan waktu serta secara otomatis menyarankan cara mengelompokkan penumpang dan mengurutkan perhentian secara optimal — mengurangi waktu tempuh dan jumlah perjalanan. Penanggung jawab dapat menerima saran, menyesuaikannya, atau menyusun penugasan secara manual.",
        "partAndroidTitle" to "Aplikasi Android",
        "partAndroidBody" to "Aplikasi Android menawarkan fungsi yang sama dengan aplikasi web, dengan pengalaman sehari-hari yang lebih lengkap. Keunggulan utamanya adalah otomatisasi geolokasi: aplikasi berjalan di latar belakang dan mencatat check-in atau check-out secara otomatis saat karyawan memasuki dan meninggalkan area terdaftar, tanpa bergantung pada peramban.\n" +
            "\n" +
            "Ia juga berfungsi tanpa internet: tanpa koneksi, catatan disimpan di ponsel dan dikirim begitu koneksi kembali, selalu dengan waktu asli. Ia juga mencakup riwayat dengan tanggal, waktu, dan lokasi setiap peristiwa, modul transportasi, dan mode darurat untuk kecelakaan.",
        "rulesTitle" to "Situasi check-in dan check-out",
        "rulesIntro" to "Situasi di bawah ini menjelaskan, langkah demi langkah, apa yang harus dilakukan sistem untuk setiap pengguna (check-in atau check-out) dalam setiap skenario lazim. Aplikasi Web dan Aplikasi Asli mengikuti aturan blok masing-masing.",
        "rulesWebTitle" to "Situasi — Aplikasi Web",
        "rulesWebBody" to "## Situasi 1 — Check-out saat menjauh\n" +
            "• Aktivitas Otomatis aktif, dengan izin lokasi penuh.\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Aplikasi memperbarui lokasi dan mendapati pengguna berada di 'Zona Check-Out' atau lebih dari 2 km dari tempat terdaftar mana pun (kecuali Zona Check-Out).\n" +
            "• Karena aktivitas terakhir adalah check-in, aplikasi melakukan check-out.\n" +
            "\n" +
            "## Situasi 2 — Sudah check-out, jauh atau di Zona Check-Out\n" +
            "• Aktivitas terakhir adalah check-out.\n" +
            "• Pengguna berada di 'Zona Check-Out' atau lebih dari 2 km dari tempat terdaftar mana pun.\n" +
            "• Tidak ada tindakan: check-out tidak diulang karena perubahan lokasi.\n" +
            "\n" +
            "## Situasi 3 — Tiba di tempat kerja (check-in)\n" +
            "• Aktivitas terakhir adalah check-out.\n" +
            "• Pengguna berada DI DALAM area terdaftar selain 'Zona Check-Out' (kecocokan nyata dengan area, bukan sekadar kedekatan).\n" +
            "• Pengguna benar-benar berada di tempat kerja (termasuk check-in pertama hari itu).\n" +
            "• Aplikasi melakukan check-in dan memperbarui lokasi ke area terdaftar yang cocok.\n" +
            "! PENTING: jika pengguna TIDAK berada di dalam area terdaftar mana pun — meskipun dekat (kurang dari 2 km dari suatu koordinat, kecuali Zona Check-Out) — aplikasi TIDAK check-in otomatis; ia hanya menampilkan 'Lokasi Tidak Terdaftar' (sama seperti Situasi 5).\n" +
            "\n" +
            "## Situasi 4 — Check-in baru (selalu)\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Pengguna berada di area terdaftar selain 'Zona Check-Out'.\n" +
            "• Aplikasi melakukan check-in baru TANPA MEMANDANG apakah lokasi berubah.\n" +
            "• Bahkan di tempat yang SAMA dengan check-in terakhir, check-in baru dilakukan untuk mencatat/memperbarui lokasi dan waktu.\n" +
            "\n" +
            "## Situasi 5 — Dekat tetapi di luar area\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Pengguna tidak berada di area terdaftar mana pun, tetapi juga tidak lebih dari 2 km dari suatu koordinat terdaftar (kecuali Zona Check-Out). Artinya, pengguna dekat dengan tempat kerja.\n" +
            "• Tidak ada tindakan: aplikasi hanya menampilkan 'Lokasi Tidak Terdaftar'.\n" +
            "\n" +
            "## Situasi 6 — Tombol 'Segarkan' setelah check-in\n" +
            "• Aplikasi sudah di latar depan; aktivitas terakhir adalah check-in.\n" +
            "• Pengguna mengetuk 'Segarkan' untuk memperbarui lokasi.\n" +
            "• Aplikasi melakukan check-in baru TANPA MEMANDANG apakah lokasi berubah, untuk mencatat/memperbarui lokasi dan waktu.\n" +
            "\n" +
            "## Situasi 7 — Meninggalkan Zona Check-Out\n" +
            "• Di latar depan; aktivitas terakhir adalah check-out; pengguna di 'Zona Check-Out' (tidak ada tindakan).\n" +
            "• Pengguna mengetuk 'Segarkan' dan aplikasi mendapati ia telah meninggalkan Zona Check-Out, menuju:\n" +
            "• Varian 7A — area terdaftar selain 'Zona Check-Out';\n" +
            "• Varian 7B — tidak ada area terdaftar, tetapi masih dekat (kurang dari 2 km, kecuali Zona Check-Out).\n" +
            "• Pada keduanya, aplikasi segera check-in, memperbarui lokasi ke area terdaftar atau, tanpa kecocokan persis, ke 'Lokasi Tidak Terdaftar'.\n" +
            "\n" +
            "## Situasi 8 — Zona Campuran\n" +
            "• Aplikasi mendeteksi posisi cocok dengan 'Zona Campuran' (pada masuk pertama atau pembacaan berurutan).\n" +
            "• Jika aktivitas relevan terakhir BUKAN di 'Zona Campuran' itu sendiri, peralihan terjadi segera: 8A — setelah check-in → check-out di 'Zona Campuran'; 8B — setelah check-out → check-in di 'Zona Campuran'.\n" +
            "• Kolom 'Interval Waktu Zona Campuran' (tab 'Pendaftaran' situs admin) adalah jeda untuk pembacaan berurutan di Zona Campuran saja: selama waktu_berlalu < interval, peralihan baru diblokir; saat >= interval, ia diizinkan lagi.\n" +
            "• Pengecualian setelah check-in di Zona Campuran: pergi ke 'Zona Check-Out' atau melampaui 'Jarak minimum untuk check-out otomatis' → check-out segera, tanpa menunggu jeda.\n" +
            "• Pengecualian setelah check-out di Zona Campuran: pergi ke area terdaftar lain (kecuali Zona Check-Out dan Zona Campuran) atau tetap dalam jarak minimum → check-in segera, mengabaikan jeda.\n" +
            "\n" +
            "## Situasi 9 — Mode manual (Aktivitas Otomatis mati)\n" +
            "• Pengguna terautentikasi; Aktivitas Otomatis MATI.\n" +
            "• Aplikasi memperbarui lokasi jika ada izin; jika tidak, ia menampilkan 'Izin ditolak'.\n" +
            "• Pengguna memilih 'check-in' atau 'check-out', 'Normal' atau 'Retroaktif', memilih 'Lokasi' (tersedia kapan pun Aktivitas Otomatis mati), dan mengetuk 'Catat'.\n" +
            "• Aplikasi mengikuti alur normal dan melakukan aktivitas sesuai pilihan.",
        "rulesNativeTitle" to "Situasi — Aplikasi Asli (Android)",
        "rulesNativeBody" to "## Situasi 1 — Check-out saat menjauh\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Pengguna di 'Zona Check-Out' atau lebih dari 2 km dari tempat terdaftar mana pun (kecuali Zona Check-Out).\n" +
            "• Aplikasi melakukan check-out.\n" +
            "\n" +
            "## Situasi 2 — Sudah check-out, jauh atau di Zona Check-Out\n" +
            "• Aktivitas terakhir adalah check-out.\n" +
            "• Pengguna di 'Zona Check-Out' atau lebih dari 2 km dari tempat terdaftar mana pun.\n" +
            "• Tidak ada tindakan: check-out tidak diulang karena perubahan lokasi.\n" +
            "\n" +
            "## Situasi 3 — Tiba di tempat kerja (check-in)\n" +
            "• Aktivitas terakhir adalah check-out.\n" +
            "• Pengguna berada DI DALAM area terdaftar selain 'Zona Check-Out' (kecocokan nyata, bukan sekadar kedekatan).\n" +
            "• Aplikasi melakukan check-in dan memperbarui lokasi ke area yang cocok.\n" +
            "! PENTING: mulai dari CHECK-OUT, jika pengguna TIDAK berada di dalam area terdaftar mana pun — meskipun dekat — aplikasi TIDAK check-in; ia hanya menampilkan 'Lokasi Tidak Terdaftar' (lihat Varian 7B). Saat aktivitas terakhir adalah CHECK-IN dan pengguna dekat tetapi di luar area, perilakunya berbeda: aplikasi melakukan check-in dengan 'Lokasi Tidak Terdaftar' sebagai perubahan (lihat Situasi 5).\n" +
            "\n" +
            "## Situasi 4 — Check-in baru hanya pada perubahan lokasi\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Pengguna di area terdaftar selain 'Zona Check-Out'.\n" +
            "• Aplikasi melakukan check-in baru HANYA jika area itu BERBEDA dari check-in terakhir.\n" +
            "• Di tempat yang SAMA dengan check-in terakhir, TIDAK ada tindakan (ini menghapus check-in ganda). Saat berpindah area, check-in baru mencatat/memperbarui lokasi dan waktu.\n" +
            "\n" +
            "## Situasi 5 — Dekat tetapi di luar area (lanjutan)\n" +
            "• Aktivitas terakhir adalah check-in.\n" +
            "• Pengguna tidak berada di area terdaftar mana pun, tetapi dekat (kurang dari 2 km dari suatu koordinat, kecuali Zona Check-Out).\n" +
            "• Karena ia meninggalkan area, aplikasi melakukan check-in dengan 'Lokasi Tidak Terdaftar', mencatat kesinambungan perjalanan.\n" +
            "• Ini hanya terjadi sebagai PERUBAHAN: jika check-in terakhir sudah 'Lokasi Tidak Terdaftar', TIDAK ada tindakan (tidak diulang).\n" +
            "\n" +
            "## Situasi 6 — Tombol 'Segarkan' setelah check-in\n" +
            "• Di latar depan; aktivitas terakhir adalah check-in.\n" +
            "• Pengguna mengetuk 'Segarkan'.\n" +
            "• Check-in baru HANYA jika lokasi BERBEDA dari check-in terakhir (aturan sama seperti Situasi 4). Di tempat yang SAMA, TIDAK ada tindakan.\n" +
            "\n" +
            "## Situasi 7 — Meninggalkan Zona Check-Out\n" +
            "• Di latar depan; aktivitas terakhir adalah check-out; pengguna di 'Zona Check-Out' (tidak ada tindakan).\n" +
            "• Pengguna mengetuk 'Segarkan' dan aplikasi memperbarui lokasi ke: Varian 7A — area terdaftar selain 'Zona Check-Out'; Varian 7B — tidak ada area terdaftar, tetapi masih dekat (kurang dari 2 km, kecuali Zona Check-Out).\n" +
            "• 7A: karena aktivitas terakhir adalah check-out, aplikasi segera check-in di area yang cocok.\n" +
            "• 7B: karena pengguna dalam keadaan check-out dan TIDAK di dalam area mana pun, aplikasi TIDAK check-in; ia hanya menampilkan 'Lokasi Tidak Terdaftar' (aturan sama seperti catatan Situasi 3).\n" +
            "\n" +
            "## Situasi 8 — Zona Campuran\n" +
            "• Aplikasi mendeteksi 'Zona Campuran' dan, jika aktivitas relevan terakhir bukan di dalamnya, beralih segera: 8A — setelah check-in → check-out di 'Zona Campuran'; 8B — setelah check-out → check-in di 'Zona Campuran'.\n" +
            "• 'Interval Waktu Zona Campuran' adalah jeda untuk pembacaan berurutan di Zona Campuran saja: selama waktu_berlalu < interval, peralihan baru diblokir; saat >= interval, ia diizinkan lagi.\n" +
            "• Pengecualian segera (mengabaikan jeda): pergi ke 'Zona Check-Out' atau melampaui jarak minimum → check-out; pergi ke area terdaftar lain atau tetap dalam jarak minimum → check-in.\n" +
            "\n" +
            "## Situasi 9 — Mode manual (Aktivitas Otomatis mati)\n" +
            "• Pengguna terautentikasi; Aktivitas Otomatis MATI.\n" +
            "• Aplikasi memperbarui lokasi jika ada izin; jika tidak, ia menampilkan 'Izin ditolak'.\n" +
            "• Pengguna memilih check-in/check-out, Normal/Retroaktif, memilih 'Lokasi', dan mengetuk 'Catat'.\n" +
            "• Aplikasi mengikuti alur normal sesuai pilihan.",
        "notesTitle" to "Catatan umum",
        "notesBody" to "## Pemicu latar depan\n" +
            "• Membuka aplikasi atau membawanya ke latar depan, dengan Aktivitas Otomatis aktif dan pengguna terautentikasi, memicu evaluasi otomatis (mesin memutuskan check-in atau check-out sesuai situasi). Hal yang sama berlaku untuk geofencing dan pemeriksaan berkala setiap 15 menit.\n" +
            "• Tidak ada check-in berkala 'membabi buta': pemeriksaan 15 menit selalu memverifikasi lokasi dan mempertahankan aturan 'lewati jika tidak ada perubahan'.\n" +
            "\n" +
            "## Check-in hanya pada perubahan lokasi\n" +
            "• Check-in otomatis hanya terjadi saat lokasi yang ditetapkan BERBEDA dari check-in terakhir. Lokasi sama → tidak ada tindakan. Aturan ini (Situasi 4 dan 6) yang MENGHAPUS check-in ganda.\n" +
            "\n" +
            "## FORMULIR per proyek\n" +
            "• Pada check-in pertama hari itu dan pada setiap check-out, formulir diisi dan dikirim SEKALI PER PROYEK tempat pengguna terdaftar (menghormati 'formulir aktif' setiap proyek). Mis.: pengguna pada proyek P80 dan P83 → dua pengiriman. Pengguna proyek tunggal → satu pengiriman.\n" +
            "\n" +
            "## Invarian check-out (dipertahankan)\n" +
            "• Check-out otomatis terjadi pada semua kasus yang dijelaskan (Zona Check-Out, jarak melampaui batas, peralihan Zona Campuran); tidak pernah ada dua check-out berturut-turut; setelah check-out, aktivitas otomatis berikutnya selalu check-in.",
    ),
)

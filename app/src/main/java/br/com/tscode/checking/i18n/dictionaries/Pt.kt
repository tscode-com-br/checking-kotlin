package br.com.tscode.checking.i18n.dictionaries

private fun d(vararg pairs: Pair<String, Any>): Map<String, Any> = mapOf(*pairs)

fun ptDictionary(): Map<String, Any> = d(
    "document" to d(
        "title" to "Checking",
        "manualTitle" to "Manual do Checking",
    ),
    "auth" to d(
        "brand" to "Checking",
        "checkFormAria" to "Formulário de registro de check-in e check-out",
        "credentialsAria" to "Identificação e senha do usuário",
        "keyLabel" to "Chave",
        "passwordLabel" to "Senha",
        "keyPlaceholder" to "Ex.: HR70",
        "passwordPlaceholder" to "3 a 10 caracteres",
        "requestRegistrationButton" to "Solicitar cadastro",
        "awaitingApproval" to "Aguardando aprovação de cadastro.",
        "registrationQueueFull" to "Fila de cadastro cheia. Informe ao administrador do sistema.",
        "settingsSpacer" to "Ajustes",
        "openSettingsAria" to "Abrir ajustes",
        "openSettingsTitle" to "Abrir ajustes",
        "waitingAuthentication" to "Aguardando autenticação.",
        "enterPasswordPrompt" to "Digite sua senha para iniciar.",
        "createPasswordPrompt" to "Digite sua chave e crie uma senha.",
        "invalidFourCharacterKey" to "Informe uma chave com 4 caracteres alfanuméricos.",
        "unknownUserDetail" to "A chave do usuario nao esta cadastrada",
        "transportAccessPrompt" to "Digite sua chave e valide a senha para acessar Transporte.",
        "showPasswordAria" to "Mostrar senha",
        "hidePasswordAria" to "Ocultar senha",
    ),
    "history" to d(
        "lastCheckinLabel" to "Último Check-In",
        "lastCheckoutLabel" to "Último Check-Out",
        "today" to "Hoje",
        "yesterday" to "Ontem",
        "dialogTitleCheckin" to "Histórico de Check-In",
        "dialogTitleCheckout" to "Histórico de Check-Out",
        "colDate" to "Data",
        "colTime" to "Hora",
        "colLocal" to "Local",
        "empty" to "Nenhum registro encontrado.",
        "back" to "Voltar",
        "loadingMessage" to "Consultando histórico...",
        "notFoundMessage" to "Nenhum registro encontrado para esta chave.",
        "noRecordsMessage" to "Nenhum check-in ou check-out registrado para esta chave.",
        "updatedMessage" to "Histórico atualizado para a chave informada.",
        "loadFailed" to "Não foi possível consultar o histórico desta chave.",
        "colActivity" to "Atividade",
        "activityCheckin" to "Check-In",
        "activityCheckout" to "Check-Out",
        "loadError" to "Não foi possível carregar o histórico.",
        "retry" to "Tentar novamente",
    ),
    "registration" to d(
        "automaticActivitiesLabel" to "Atividades Automáticas",
        "sectionTitle" to "Registro",
        "checkinLabel" to "Check-In",
        "checkoutLabel" to "Check-Out",
        "transportLabel" to "Transporte",
        "informeTitle" to "Informe",
        "informeNormalLabel" to "Normal",
        "informeRetroativoLabel" to "Retroativo",
        "submitButton" to "Registrar",
        "checkInLowerLabel" to "check-in",
        "checkOutLowerLabel" to "check-out",
        "disableAutomaticActivitiesForManualSubmit" to "Desative Atividades Automáticas para registrar manualmente.",
        "selectLocationBeforeSubmit" to "Selecione uma localização antes de registrar.",
    ),
    "settings" to d(
        "title" to "Ajustes",
        "languageLabel" to "Idioma",
        "resetPasswordLabel" to "Alterar Senha",
        "allowLocationLabel" to "Permitir localização",
        "allowAudioVideoLabel" to "Permitir Audio & Video",
        "notificationsLabel" to "Avisos",
        "supportLabel" to "Suporte",
        "manualLabel" to "Instruções de Uso",
        "aboutLabel" to "Sobre",
        "activitiesLabel" to "Atividades",
        "backButton" to "Voltar",
        "groupAutoActivities" to "Atividades Automáticas",
        "groupPreferences" to "Preferências",
        "groupHelp" to "Ajuda",
        "statusOn" to "Ativadas",
        "statusAttention" to "Atenção",
        "statusOff" to "Desativadas",
    ),
    "notifications" to d(
        "title" to "Avisos",
        "intro" to "Habilite as notificações para ficar ciente sempre que o Checking realizar alguma atividade automaticamente ou para ficar por dentro de acontecimentos importantes. Habilite notificações 'push' para:",
        "checkboxActivities" to "notificarem quando uma atividade for realizada automaticamente.",
        "checkboxScheduledPause" to "saber quando o aplicativo iniciar ou finalizar o modo 'Pausa Programada'.",
        "checkboxAccident" to "saber quando houver um acidente reportado.",
        "backButton" to "Voltar",
    ),
    "permissions" to d(
        "title" to "Permissões",
        "locationText" to "O compartilhamento da localização exata/precisa permite que o aplicativo identifique em qual região cadastrada no sistema o usuário encontra-se. Marque a opção 'SEMPRE PERMITIR' e ative a opção 'Usar localização precisa'.",
        "locationButton" to "Localização Exata",
        "cameraMicText" to "A câmera e o microfone do dispositivo somente serão utilizados caso o usuário deseje registrar um vídeo de um acidente ocorrido, para a informação dos administradores.",
        "cameraButton" to "Câmera",
        "microphoneButton" to "Microfone",
        "autoStartText" to "Ativar a opção \"Iniciar Automaticamente\" permite que o sistema Android inicie o Checking automaticamente todas as vezes em que o dispositivo for ligado ou reiniciado.",
        "autoStartButton" to "Início Automático",
        "batteryText" to "Se houver algum modo de economia de bateria ativo, as atividades em segundo plano poderão ser prejudicadas.",
        "batteryButton" to "Restrições de Bateria",
        "backgroundText" to "Permitir que o aplicativo permaneça operante em segundo plano fará com que as atividades sejam realizadas mesmo quando o dispositivo estiver bloqueado.",
        "backgroundButton" to "Segundo Plano",
        "backgroundOpsText" to "Para o Checking funcionar perfeitamente em segundo plano — realizando as atividades mesmo com a tela bloqueada ou após o app ser fechado, e reiniciando junto com o dispositivo — conceda as permissões a seguir: rodar em segundo plano, remover restrições de bateria e iniciar automaticamente.",
        "backgroundOpsButton" to "Funcionamento em Segundo Plano",
        "notificationsText" to "As notificações informarão ao usuário quando as atividades serão realizadas.",
        "notificationsButton" to "Notificações",
        "exactAlarmText" to "Os alarmes exatos permitem que o Checking retome as atividades automáticas no horário exato em que a Pausa Programada termina.",
        "exactAlarmButton" to "Alarmes Exatos",
        "statusButton" to "Status de Permissões",
        "statusTitle" to "Status de Permissões",
        "backButton" to "Voltar",
        "statusLocation" to "Localização",
        "statusCameraMic" to "Câmera e Microfone",
        "statusAutoStart" to "Auto-Start",
        "statusBattery" to "Restrições de Bateria",
        "statusBackground" to "Segundo Plano",
        "statusNotifications" to "Notificações",
        "statusExactAlarm" to "Alarmes Exatos",
        "locationPrecise" to "permitida precisa",
        "locationPreciseNoBackground" to "precisa, mas sem 'Sempre Permitir'",
        "locationImprecise" to "permitida imprecisa",
        "locationDenied" to "não permitida",
        "cameraMicGranted" to "permitidos",
        "cameraMicDenied" to "não permitidos",
        "autoStartOn" to "ativado",
        "autoStartOff" to "não ativado",
        "batteryRestricted" to "restrito",
        "batteryUnrestricted" to "não restrito",
        "backgroundAllowed" to "permitido",
        "backgroundDisallowed" to "não permitido",
        "notificationsAllowed" to "permitidas",
        "notificationsDisallowed" to "não permitidas",
        "exactAlarmAllowed" to "permitidos",
        "exactAlarmDisallowed" to "não permitidos",
    ),
    "passwordDialog" to d(
        "titleChange" to "Alterar Senha",
        "titleRegister" to "Cadastrar Senha",
        "oldPasswordLabel" to "Senha Antiga",
        "newPasswordLabel" to "Nova Senha",
        "confirmPasswordLabel" to "Confirme Senha",
        "backButton" to "Voltar",
        "submitChangeButton" to "Alterar",
        "submitRegisterButton" to "Salvar",
        "changingStatus" to "Alterando senha...",
        "savingStatus" to "Salvando senha...",
        "validatingStatus" to "Senha sendo verificada.",
        "oldPasswordInvalid" to "A senha antiga deve ter entre 3 e 10 caractéres.",
        "newPasswordInvalid" to "A nova senha deve ter entre 3 e 10 caractéres.",
        "confirmMismatch" to "A confirmação da nova senha não confere.",
        "changeFailed" to "Não foi possível alterar a senha.",
        "validationFailed" to "Não foi possível validar a senha.",
        "statusLoadFailed" to "Não foi possível consultar o status da senha.",
    ),
    "registrationDialog" to d(
        "title" to "Solicitar Cadastro",
        "note" to "Preencha as informações abaixo para utilizar o sistema Checking.",
        "keyLabel" to "Chave",
        "fullNameLabel" to "Nome Completo",
        "projectsLabel" to "Projetos",
        "projectsHint" to "Selecione um ou mais projetos.",
        "emailLabel" to "E-Mail",
        "emailPlaceholder" to "Opcional",
        "passwordLabel" to "Senha",
        "confirmPasswordLabel" to "Confirma Senha",
        "backButton" to "Voltar",
        "submitButton" to "Enviar",
        "loadingProjects" to "Carregando projetos...",
        "noProjectsAvailable" to "Nenhum projeto está disponível no momento.",
        "fullNameRequired" to "Informe o nome completo.",
        "emailInvalid" to "Informe um e-mail válido ou deixe o campo em branco.",
        "passwordInvalid" to "A senha deve ter entre 3 e 10 caracteres.",
        "confirmMismatch" to "A confirmação da nova senha não confere.",
        "submittingStatus" to "Enviando solicitação de cadastro...",
        "successStatus" to "Cadastro concluído com sucesso.",
        "submitFailed" to "Não foi possível enviar a solicitação de cadastro.",
    ),
    "location" to d(
        "title" to "Local",
        "waitingLabel" to "Aguardando localização.",
        "refreshLabel" to "Atualizar localização",
        "refreshBusyLabel" to "Atualizando localização",
        "unavailableShort" to "Indisponível",
        "unavailableLabel" to "Localização indisponível",
        "unavailableMessage" to "Não foi possível consultar a localização neste momento.",
        "noPermissionLabel" to "Sem Permissão",
        "timeoutLabel" to "Tempo esgotado",
        "timeoutMessage" to "A busca pela localização demorou mais do que o esperado.",
        "detectingLabel" to "Detectando...",
        "exactConfirmationBrowser" to "Aguardando a confirmação da localização exata pelo navegador.",
        "exactConfirmationApp" to "Aguardando a confirmação da localização exata pelo atalho/app.",
        "updatingDeviceLocation" to "Atualizando a localização atual do aparelho.",
        "secureContextRequired" to "A localização precisa requer uma conexão segura (HTTPS).",
        "browserUnsupported" to "Este navegador não oferece suporte à localização precisa.",
        "permissionBlocked" to "A permissão de localização está bloqueada no navegador. Libere o acesso ao site nas configurações do navegador.",
        "captureRequiresSupport" to "A captura de localização requer HTTPS e suporte do navegador.",
        "noValidPosition" to "Não foi possível obter uma posição válida do aparelho.",
        "searchingPrecision" to "Buscando precisão suficiente...",
        "completionStatus" to "Atualização da localização concluída.",
        "completionStatusWithDetail" to "Atualização da localização concluída. {detail}",
        "browserContextLabel" to "neste navegador",
        "appContextLabel" to "neste atalho/app",
        "browserSourceLabel" to "pelo navegador",
        "appSourceLabel" to "pelo atalho/app",
        "currentAccuracyLabel" to "Precisão atual",
        "accuracyPrefix" to "Precisão",
        "accuracyTemplate" to "Precisão {accuracy}",
        "accuracyLimitTemplate" to "Limite {limit} m",
        "accuracyCombinedTemplate" to "Precisão {accuracy} / Limite {limit} m",
        "noKnownLocations" to "Sem localizações cadastradas",
        "defaultManualLocationLabel" to "Escritório Principal",
        "accuracyFallbackManualLocationLabel" to "Precisao Insuficiente",
        "outsideWorkplaceLabel" to "Fora do Local de Trabalho",
        "unregisteredLocationLabel" to "Localização não Cadastrada",
        "mixedZoneLabel" to "Zona Mista",
        "checkoutZoneLabel" to "Zona de checkout",
        "manualSelectLabel" to "Localização manual",
        "manualSelectPlaceholder" to "Selecione um local",
        "selectManualLocation" to "Selecione uma localização antes de registrar.",
    ),
    "projects" to d(
        "label" to "Projetos",
        "changeButton" to "Alterar",
        "loadingProjects" to "Carregando projetos...",
        "updatingProjects" to "Atualizando projetos...",
        "noneAvailableShort" to "Nenhum projeto disponível",
        "noneAvailableSentence" to "Nenhum projeto disponível.",
        "noneAvailableNow" to "Nenhum projeto está disponível no momento.",
        "selectAtLeastOne" to "Selecione ao menos um projeto.",
        "userProjectsAria" to "Projetos do usuário",
        "registrationProjectsAria" to "Projetos do cadastro",
        "updatedSuccess" to "Projetos atualizados com sucesso.",
        "loadFailed" to "Não foi possível carregar os projetos.",
        "userProjectsLoadFailed" to "Não foi possível carregar os projetos do usuário.",
        "updateFailed" to "Não foi possível atualizar os projetos.",
        "noActiveProject" to "Nenhum projeto ativo selecionado.",
    ),
    "transport" to d(
        "title" to "Agendamento de Transporte",
        "backToMainAria" to "Voltar para a tela principal",
        "editAddressAria" to "Editar endereço",
        "addressToggleLabel" to "Endereço:",
        "addressLabel" to "Endereço:",
        "zipLabel" to "Código ZIP:",
        "addressPlaceholder" to "Bloco (se houver), rua e número.",
        "zipPlaceholder" to "Apenas 6 números",
        "addressBackButton" to "Voltar",
        "addressSubmitButton" to "Cadastrar",
        "optionInstruction" to "Selecione o tipo de transporte para continuar.",
        "historyTitle" to "Solicitações ativas",
        "historyButtonLabel" to "Histórico",
        "historyPanelTitle" to "Histórico de Solicitações",
        "historyCloseButton" to "Fechar",
        "kinds" to d(
            "regular" to "Dias de Semana",
            "weekend" to "Fins de Semana",
            "extra" to "Transporte Extra",
        ),
        "statusLabels" to d(
            "available" to "Sem solicitação",
            "pending" to "Pendente",
            "confirmed" to "Confirmado",
            "realized" to "Realizado",
            "rejected" to "Rejeitado",
            "cancelled" to "Cancelado",
        ),
        "weekdays" to d(
            "short" to d(
                "0" to "Seg",
                "1" to "Ter",
                "2" to "Qua",
                "3" to "Qui",
                "4" to "Sex",
                "5" to "Sáb",
                "6" to "Dom",
            ),
            "full" to d(
                "0" to "Segunda-feira",
                "1" to "Terça-feira",
                "2" to "Quarta-feira",
                "3" to "Quinta-feira",
                "4" to "Sexta-feira",
                "5" to "Sábado",
                "6" to "Domingo",
            ),
        ),
        "requestBuilder" to d(
            "selectDaysLabel" to "Selecione os dias:",
            "regularSubtitle" to "Selecione os dias úteis desejados para esta solicitação.",
            "weekendSubtitle" to "Selecione os dias de fim de semana desejados para esta solicitação.",
            "extraSubtitle" to "Confira a data e o horário antes de solicitar.",
            "dateLabel" to "Data:",
            "timeLabel" to "Horário:",
            "timePlaceholder" to "Selecionar horário",
            "pickerConfirm" to "OK",
            "pickerCancel" to "Cancelar",
            "backToHour" to "Voltar para a hora",
            "backButton" to "Voltar",
            "submitButton" to "Solicitar",
            "requestUnavailable" to "Solicitacao de transporte indisponivel.",
            "addressRequired" to "Cadastre um endereco completo antes de solicitar o transporte.",
            "dateRequiredExtra" to "Informe a data do transporte extra.",
            "timeRequiredExtra" to "Informe o horário do transporte extra.",
            "dayRequired" to "Selecione ao menos um dia para solicitar o transporte.",
            "conflictGeneric" to "Ja existe uma solicitacao de transporte ativa para essa data.",
            "conflictByDate" to "Ja existe uma solicitacao de transporte ativa para {serviceDateLabel}.",
        ),
        "summary" to d(
            "noRequestRecorded" to "Nenhuma solicitação registrada.",
            "noActiveRequests" to "Nenhuma solicitação ativa.",
            "noRequestStatus" to "Sem solicitação",
            "waitingAllocation" to "Aguardando alocação de transporte.",
            "vehicleAllocated" to "Veículo alocado.",
            "scheduleUnavailable" to "Programação indisponível.",
            "requestClosed" to "Solicitação encerrada.",
            "whenRequestExists" to "Quando houver uma solicitação, ela aparecerá aqui.",
            "whenAllocated" to "Quando você for alocado em um veículo, as informações aparecerão aqui.",
            "departureAndLimit" to "Partida {departureTime} • Limite {deadlineTime}",
            "limitOnly" to "Limite {deadlineTime}",
        ),
        "detail" to d(
            "title" to "Detalhes da Solicitação",
            "genericTitle" to "Transporte",
            "waitingAllocation" to "Aguardando alocação de transporte.",
            "whenAllocated" to "Quando você for alocado em um veículo, as informações aparecerão aqui.",
            "inactive" to "Esta solicitação não está mais ativa.",
            "confirmed" to "Transporte confirmado.",
            "realized" to "Transporte realizado.",
            "vehicleTypeLabel" to "Tipo de Veículo",
            "vehiclePlateLabel" to "Placa do Veículo",
            "vehicleColorLabel" to "Cor do Veículo",
            "departureDateLabel" to "Data de Partida",
            "departureTimeLabel" to "Hora de Partida",
            "unavailableValue" to "Indisponível",
        ),
        "actions" to d(
            "markRealized" to "Realizado",
            "cancel" to "Cancelar",
            "cancelling" to "Cancelando...",
        ),
        "messages" to d(
            "invalidKeyBeforeAddress" to "Informe uma chave válida antes de atualizar o endereço.",
            "invalidKeyBeforeRequest" to "Informe uma chave válida antes de solicitar o transporte.",
            "requestFailed" to "Não foi possível solicitar {requestLabel}.",
            "loadFailed" to "Não foi possível consultar o transporte.",
            "addressUpdated" to "Endereço atualizado com sucesso.",
            "addressUpdateFailed" to "Não foi possível atualizar o endereço.",
            "cancelSuccess" to "Solicitação de transporte cancelada.",
            "cancelFailed" to "Não foi possível cancelar a solicitação.",
            "requestMarkedRealized" to "Solicitação marcada como realizada.",
            "accessRequiresAuthentication" to "Digite sua chave e valide a senha para acessar Transporte.",
        ),
    ),
    "status" to d(
        "validationError" to "Erro de validação.",
        "apiCommunicationFailure" to "Falha de comunicação com a API.",
        "passwordVerifying" to "Senha sendo verificada.",
        "authenticationCompleted" to "Autenticação concluída.",
        "updatingApp" to "Atualizando a aplicação...",
        "userAuthenticated" to "Usuário autenticado. Iniciando atualizações.",
        "applicationUpdated" to "Aplicação atualizada com sucesso.",
        "applicationUpdateFailed" to "Não foi possível atualizar a aplicação neste momento.",
        "checkinCompleted" to "Check-In concluído.",
        "checkoutCompleted" to "Check-Out concluído.",
        "savedOffline" to "Salvo offline. Será sincronizado quando houver conexão.",
        "automaticCheckinCompleted" to "Check-In automático concluído.",
        "automaticCheckoutCompleted" to "Check-Out automático concluído.",
        "updatingActivitiesSequence" to "Atualizando as atividades.....",
        "updatingLocationSequence" to "Atualizando a localização.....",
        "runningAutomaticActivitySequence" to "Realizando check-in ou check-out, se aplicável.....",
        "automaticUpdatesRunning" to "Atualização em andamento.",
        "automaticUpdatesCompletedWithActivity" to "Atualizações concluídas com {activity} realizado.",
        "automaticUpdatesCompletedWithoutActivity" to "Atualizações concluídas sem atividades realizadas.",
        "automaticUpdatesFailed" to "Não foi possível concluir as atualizações automáticas neste momento.",
        "automaticActivitiesDisabled" to "O modo 100% manual foi ativado.",
        "operationFailed" to "Não foi possível concluir a operação.",
        "submitFailed" to "Não foi possível registrar o check-in/out neste momento.",
    ),
    "manual" to d(
        "eyebrow" to "Checking Web • Guia de uso",
        "heading" to "Instruções de Uso",
        "introPrimary" to "Este manual resume os fluxos principais de autenticação, registro, localização, transporte e suporte do Checking Web.",
        "introSecondary" to "Use esta página como referência rápida para entender o que o aplicativo faz automaticamente e quais ações continuam sob controle do usuário.",
        "currentLanguageLabel" to "Idioma desta página",
        "availabilityNote" to "Nesta fase, o manual completo está disponível em português e inglês.",
        "highlights" to d(
            "accessTitle" to "Acesso guiado",
            "accessBody" to "O app decide automaticamente quando pedir cadastro de usuário, criação de senha ou autenticação normal.",
            "locationTitle" to "Localização acompanhada",
            "locationBody" to "As permissões, a precisão do GPS e o fallback manual influenciam diretamente o registro de presença.",
            "supportTitle" to "Ajuda rápida",
            "supportBody" to "Ajustes centraliza idioma, senha, localização, suporte por WhatsApp e o acesso a esta documentação.",
        ),
        "tocTitle" to "Mapa do manual",
        "tocAriaLabel" to "Navegação do manual",
        "snapshotSlotLabel" to "Slot de captura",
        "toc" to d(
            "overview" to "Visão geral",
            "authFlow" to "Fluxo de autenticação",
            "userRegistration" to "Cadastro automático de usuário",
            "passwordRegistration" to "Cadastro automático de senha",
            "login" to "Login",
            "attendance" to "Check-in e check-out",
            "projectSelection" to "Seleção de projetos",
            "location" to "Permissão de localização",
            "automaticActivities" to "Atividades automáticas",
            "transport" to "Transporte",
            "passwordChange" to "Troca de senha",
            "settings" to "Ajustes",
            "support" to "Suporte",
            "faq" to "Problemas comuns e FAQ",
        ),
        "sections" to d(
            "overview" to d(
                "title" to "Visão geral",
                "lead" to "O Checking Web reúne autenticação, registro de presença, contexto de localização e acesso a transporte numa única superfície otimizada para celular.",
                "item1" to "A tela principal mostra a chave, a senha, o histórico recente, o formulário de registro e o atalho para Transporte.",
                "item2" to "Os fluxos de ajuda aparecem automaticamente quando a chave ainda não existe ou quando o usuário precisa criar a primeira senha.",
                "item3" to "O menu Ajustes reúne ações secundárias para reduzir ruído visual na área principal.",
                "figureCaption" to "Tela principal do Checking Web com a área de autenticação e o formulário de registro de presença.",
            ),
            "authFlow" to d(
                "title" to "Fluxo de autenticação",
                "lead" to "A aplicação consulta o status da chave assim que ela atinge quatro caracteres válidos e decide qual assistência exibir.",
                "item1" to "Se a chave não existir, o fluxo muda para cadastro de usuário sem esperar um clique adicional.",
                "item2" to "Se a chave existir, mas ainda não tiver senha, o fluxo abre diretamente o cadastro de senha.",
                "item3" to "Se a chave e a senha já existirem, a interface continua no caminho de login normal.",
                "note" to "Fechar um modal manualmente não cria um loop infinito: o sistema só tenta abrir de novo quando a chave ou o estado relevante realmente mudam.",
            ),
            "userRegistration" to d(
                "title" to "Cadastro automático de usuário",
                "lead" to "Quando a chave não existe no banco, o app abre o formulário de autoatendimento para o novo usuário concluir o cadastro.",
                "item1" to "O formulário solicita chave, nome completo, projetos, e-mail opcional, senha e confirmação de senha.",
                "item2" to "Os projetos são carregados da API e o usuário pode marcar um ou mais itens válidos.",
                "item3" to "Depois do envio bem-sucedido, a sessão web é autenticada e o app continua liberado para uso.",
                "figureCaption" to "Modal de cadastro de novo usuário aberto automaticamente para uma chave inexistente.",
            ),
            "passwordRegistration" to d(
                "title" to "Cadastro automático de senha",
                "lead" to "Se a chave já existir, mas não houver senha cadastrada, o Checking Web entra no modo de criação de senha.",
                "item1" to "O modal reaproveita o mesmo componente de senha, mas esconde o campo de senha antiga.",
                "item2" to "O usuário informa a nova senha, confirma o valor e conclui o primeiro acesso.",
                "item3" to "Após salvar, o backend autentica a sessão web e a tela principal passa a aceitar as demais operações.",
                "figureCaption" to "Modal de criação da primeira senha para um usuário já conhecido pelo sistema.",
            ),
            "login" to d(
                "title" to "Login",
                "lead" to "Usuários já cadastrados digitam a chave, informam a senha e deixam o app validar a autenticação antes de registrar presença ou abrir Transporte.",
                "item1" to "A senha pode ser verificada de forma automática pelo fluxo já existente do shell principal.",
                "item2" to "Se o usuário alterar a senha digitada depois de autenticar, o app volta a se proteger até que a nova senha seja validada novamente.",
                "item3" to "As mensagens de status da interface ajudam a distinguir \"aguardando senha\", \"verificando\" e \"autenticado\".",
            ),
            "attendance" to d(
                "title" to "Check-in e check-out",
                "lead" to "Com a autenticação válida, o usuário escolhe o tipo de registro, revisa o contexto e envia a operação na própria tela principal.",
                "item1" to "O formulário diferencia check-in e check-out e pode bloquear envio manual quando as atividades automáticas estiverem dominando o fluxo.",
                "item2" to "O histórico visível na parte superior ajuda a confirmar o último evento registrado antes de enviar um novo evento.",
                "item3" to "Mensagens de sucesso ou falha aparecem na área de status após cada tentativa.",
                "figureCaption" to "Exemplo de estado bem-sucedido após um registro de presença.",
            ),
            "projectSelection" to d(
                "title" to "Seleção de projetos",
                "lead" to "O app usa projetos tanto no cadastro inicial quanto na rotina diária, para limitar o contexto do usuário e das localizações disponíveis.",
                "item1" to "No cadastro, o usuário marca um ou mais projetos válidos antes de concluir a criação da conta.",
                "item2" to "Depois de autenticado, o painel principal mostra os projetos atuais e permite atualização quando essa capacidade estiver disponível.",
                "item3" to "O projeto ativo influencia listas de localizações, histórico contextual e outras áreas dependentes do escopo do usuário.",
                "figureCaption" to "Exemplo do bloco de projetos no contexto autenticado, com resumo dos escopos ativos.",
            ),
            "location" to d(
                "title" to "Permissão de localização e comportamento do GPS",
                "lead" to "A posição do aparelho é usada para determinar contexto operacional, acionar fluxos automáticos e orientar o usuário quando a precisão não é suficiente.",
                "item1" to "A aplicação depende de HTTPS, suporte do navegador e permissão ativa para consultar localização precisa.",
                "item2" to "Quando a permissão está negada ou indisponível, o app mostra mensagens claras e pode limitar o fluxo ao fallback manual permitido.",
                "item3" to "Quando a localização é obtida com sucesso, a interface atualiza precisão, local reconhecido e ações automáticas relacionadas.",
                "figureCaptionDenied" to "Exemplo de tela quando a permissão de localização ainda não está disponível.",
                "figureCaptionGranted" to "Exemplo de tela depois que a localização precisa foi concedida com sucesso.",
            ),
            "automaticActivities" to d(
                "title" to "Atividades automáticas",
                "lead" to "O app também consegue disparar check-in, check-out e atualizações relacionadas quando o contexto de localização muda da forma esperada.",
                "item1" to "Esse mecanismo depende de autenticação válida, leituras de localização e regras internas que evitam transições indevidas.",
                "item2" to "Quando as atividades automáticas estão controlando o fluxo, alguns campos manuais ficam ocultos ou bloqueados para proteger a consistência do registro.",
                "item3" to "Se a precisão cair abaixo do necessário, o app pode reabrir opções de fallback manual em vez de assumir uma decisão arriscada.",
            ),
            "transport" to d(
                "title" to "Acesso ao Transporte",
                "lead" to "Depois da autenticação, o usuário pode abrir o módulo de Transporte para solicitar viagens, consultar status e revisar detalhes da solicitação mais recente.",
                "item1" to "O acesso continua protegido pela mesma chave e senha validadas no shell principal.",
                "item2" to "O módulo inclui cadastro de endereço, solicitação por tipo de transporte e um resumo do status atual.",
                "item3" to "Quando houver alocação de veículo, a tela mostra os principais detalhes operacionais para o usuário.",
                "figureCaption" to "Exemplo da superfície de Transporte acessada a partir do shell do Checking Web.",
            ),
            "passwordChange" to d(
                "title" to "Resetar ou trocar senha",
                "lead" to "A alteração de senha saiu da linha principal de autenticação e agora fica concentrada em Ajustes.",
                "item1" to "Abra Ajustes, toque em Alterar Senha e use o modal existente de troca de senha.",
                "item2" to "O fluxo pede senha antiga, nova senha e confirmação, preservando as validações já existentes.",
                "item3" to "O botão só fica habilitado quando o usuário já está autenticado e possui uma senha cadastrada.",
                "figureCaption" to "Fluxo de troca de senha iniciado a partir de Ajustes > Alterar Senha.",
            ),
            "settings" to d(
                "title" to "Ajustes",
                "lead" to "O ícone de engrenagem abre um ponto central para preferências e ações secundárias, sem poluir a linha de autenticação.",
                "item1" to "Idioma altera os textos visíveis do aplicativo principal e preserva a preferência no navegador.",
                "item2" to "Permitir localização reaproveita o pipeline existente para solicitar novamente o acesso preciso quando isso ainda for necessário.",
                "item3" to "Suporte e Sobre usam esse mesmo ponto de entrada para abrir o WhatsApp e esta documentação.",
                "figureCaption" to "Widget Ajustes com as novas ações secundárias centralizadas.",
            ),
            "support" to d(
                "title" to "Suporte",
                "lead" to "Quando o usuário precisa de ajuda humana, Ajustes > Suporte monta uma conversa no WhatsApp com a chave já incluída na primeira mensagem.",
                "item1" to "O link usa o número oficial configurado no frontend do Checking Web.",
                "item2" to "A mensagem inicial é preparada automaticamente para reduzir atrito e acelerar o atendimento.",
                "item3" to "Se nenhuma chave válida estiver disponível, o botão de suporte permanece desabilitado por segurança.",
            ),
            "faq" to d(
                "title" to "Problemas comuns e FAQ",
                "lead" to "Use estas respostas rápidas quando a interface parecer travada ou quando o fluxo não estiver avançando como esperado.",
                "q1" to "Por que o app abriu um cadastro sozinho?",
                "a1" to "Isso acontece quando a chave não existe ou quando a conta ainda não possui senha. O novo fluxo evita cliques extras e leva o usuário direto para a ação correta.",
                "q2" to "Por que o botão Permitir localização está desabilitado?",
                "a2" to "Porque a aplicação já entende que a permissão precisa está ativa, ou porque uma atualização de localização já está em andamento.",
                "q3" to "O que fazer se o Transporte não abrir?",
                "a3" to "Confirme primeiro se a chave e a senha foram autenticadas. Se o problema persistir, abra Suporte para enviar sua chave ao atendimento.",
            ),
            "scheduledPause" to d(
                "title" to "Pausa Programada",
                "lead" to "A Pausa Programada economiza bateria suspendendo as atividades automáticas durante um período (por exemplo, à noite).",
                "item1" to "Em Ajustes, toque em Pausa Programada.",
                "item2" to "Ative a opção e defina os horários De e Até (por exemplo, das 22:00 às 06:00).",
                "item3" to "Se quiser, marque também Suspender aos sábados e/ou Suspender aos domingos. Durante a pausa, o app não realiza nenhuma atividade automática e retoma sozinho ao fim do período.",
            ),
            "accident" to d(
                "title" to "Em caso de acidente",
                "lead" to "O Modo Acidente é um recurso de segurança. Use-o apenas em uma emergência real.",
                "item1" to "Qualquer usuário pode abrir o Modo Acidente; isso avisa, em tempo real, todos os usuários do mesmo projeto.",
                "item2" to "Informe sua situação e zona: em segurança, no local do acidente mas em segurança, ou no local do acidente e precisando de ajuda. Se possível, grave um vídeo do local — ele é enviado em tempo real para o painel do administrador.",
                "item3" to "O botão Acionar Serviço de Emergência liga para o serviço de emergência local, informando o acidente e o local no idioma da região.",
            ),
        ),
        "figures" to d(
            "authShellAlt" to "Tela principal do Checking Web com campos de chave e senha.",
            "userRegistrationAlt" to "Formulário de cadastro de novo usuário do Checking Web.",
            "passwordRegistrationAlt" to "Modal de cadastro inicial de senha para um usuário existente sem senha.",
            "settingsModalAlt" to "Widget Ajustes do Checking Web aberto com idioma, senha, localização, suporte e sobre.",
            "passwordChangeAlt" to "Modal de alteração de senha aberto a partir de Ajustes.",
            "locationDeniedAlt" to "Estado do Checking Web com permissão de localização negada ou indisponível.",
            "locationGrantedAlt" to "Estado do Checking Web com localização precisa disponível e compartilhada.",
            "projectSelectionAlt" to "Área de seleção de projetos no Checking Web.",
            "transportScreenAlt" to "Tela do módulo de Transporte dentro do ecossistema Checking Web.",
            "checkSuccessAlt" to "Estado de sucesso após um check-in ou check-out realizado no Checking Web.",
        ),
    ),
    "accident" to d(
        "button" to d(
            "report" to "Reportar Acidente",
            "reported" to "Acidente Reportado",
        ),
        "wizard" to d(
            "selectProject" to "Selecione o Projeto",
            "selectLocation" to "Local do Acidente",
            "yourSituation" to "Sua Situação:",
            "confirmTitle" to "Confirmação de Acidente",
            "confirmTextTemplate" to "Você está prestes a reportar um acidente na localização {location} do projeto {project}.",
            "conflictAlreadyActive" to "Já existe um acidente ativo no momento.",
        ),
        "notification" to d(
            "bannerTemplate" to "Acidente Reportado no projeto {project}!",
        ),
        "inquiry" to d(
            "title" to "Estou em:",
            "titleAfter" to "Sua Situação",
            "safetyZone" to "Zona de Segurança",
            "accidentZone" to "Zona de Acidente",
            "imOk" to "Estou bem.",
            "needHelp" to "Preciso de Ajuda!",
        ),
        "confirm" to d(
            "safety" to "Você confirma que está fora de perigo?",
            "accidentOk" to "Você confirma que está na zona do acidente e que está fora de perigo?",
            "help" to "Você confirma que está na zona do acidente e que precisa de ajuda?",
        ),
        "actions" to d(
            "title" to "Ações de Emergência",
            "audioVideo" to "Audio & Video",
            "reportNew" to "Reportar Novo Acidente",
            "back" to "Voltar",
        ),
        "ack" to d(
            "title" to "Acidente Reportado",
            "checkinReminder" to "Realize check-in IMEDIATAMENTE, caso esteja no ambiente de trabalho.",
            "button" to "Ciente",
        ),
        "situationSent" to "Situação atual enviada.",
        "triggerEmergency" to "Acionar Serviço de Emergência",
        "description" to d(
            "title" to "Descrição Detalhada",
            "placeholder" to "Descreva o ocorrido (opcional, máx. 500 caracteres)...",
        ),
        "fallback" to d(
            "manualCheckin" to "Situação de Acidente. Realize o check-in manual IMEDIATAMENTE.",
        ),
        "video" to d(
            "sending" to "Enviando o registro...",
            "sent" to "Registro enviado com sucesso.",
            "error" to "Erro: registro não enviado.",
            "permissionRequired" to "É necessário permitir o acesso à câmera e ao microfone para gravar o vídeo.",
        ),
        "settings" to d(
            "permitAudioVideo" to "Permitir Audio & Video",
            "permitted" to "Audio & Video permitido",
        ),
        "emergency" to d(
            "callInitiated" to "Ligação de emergência N.º {label} iniciada.",
            "alreadyCalled" to "A ligação de emergência já foi realizada.",
            "callFailed" to "Não foi possível acionar a ligação de emergência.",
        ),
    ),
    "support" to d(
        "phoneNumber" to "5521992174446",
        "messageTemplate" to "Preciso de ajuda com a aplicação Checking Web. Minha chave é {chave}.",
    ),
    "autoActivities" to d(
        "title" to "Atividades Automáticas",
        "explanation" to "Se habilitada, esta opção permite que o Checking realize atividades de check-in e check-out em segundo plano, sem aviso prévio, com base na localização do usuário. É importante lembrar que, em momento algum, as coordenadas GPS obtidas são compartilhadas com terceiros, servindo apenas para identificar se o usuário encontra-se dentro de alguma localização cadastrada para o seu projeto.",
        "enable" to "Habilitar Atividades Automáticas",
        "insufficientPermissions" to "Permissões mínimas não concedidas. Em Ajustes › Permissões, conceda Notificações e Localização Exata para ativar as atividades automáticas.",
        "reducedReliability" to "Atividades automáticas ativas. Para funcionarem de forma confiável em segundo plano, conceda a Localização como 'Permitir o tempo todo' e desative a otimização de bateria em Ajustes › Permissões.",
        "permNotifications" to "Notificações",
        "permLocationAllTime" to "Localização 'o tempo todo'",
        "permBattery" to "Bateria sem restrição",
        "permAutoStart" to "Iniciar com o aparelho",
        "nudgeQuestion" to "Quer que o Checking faça check-in e check-out automaticamente, com base na sua localização?",
        "nudgeActivate" to "Ativar agora",
        "nudgeLater" to "Agora não",
        "reviewPermissions" to "Revisar Permissões",
        "permissionsNotice" to "Para que as atividades automáticas possam ocorrer de forma confiável, todas as permissões solicitadas devem ser concedidas ao Checking.",
        "permStep" to d(
            "backgroundLocationRationale" to "Para monitorar sua localização em segundo plano, o Checking precisa da permissão 'Permitir o tempo todo'. Na próxima tela, selecione essa opção em Localização.",
            "batteryRationale" to "Para funcionar de forma confiável quando a tela estiver desligada, o Checking precisa estar isento das otimizações de bateria. Confirme na próxima tela.",
            "oemGuidanceTitle" to "Permita a execução em segundo plano",
            "oemGuidanceBody" to "Neste dispositivo, pode ser necessário ativar a inicialização automática ou remover restrições de bateria para o Checking nas configurações do fabricante. Abra as configurações e localize o Checking na lista de aplicativos.",
            "permanentlyDeniedNotice" to "Permissão negada permanentemente. Abra as configurações do aplicativo para concedê-la manualmente.",
        ),
        "close" to "Fechar",
        "notification" to d(
            "serviceTitle" to "Checking — Atividades automáticas",
            "serviceBody" to "Monitorando sua localização para check-in/check-out automáticos.",
            "checkinTitle" to "Check-in realizado",
            "checkoutTitle" to "Check-out realizado",
            "eventBody" to "{local} • {hora}",
            "brandTitle" to "Checking",
            "checkinMessage" to "Check-In realizado.",
            "checkoutMessage" to "Check-Out realizado.",
            "pauseStartMessage" to "Checking em pausa.",
            "pauseEndMessage" to "Checking em atividade.",
            "accidentMessage" to "Checking: acidente reportado!",
            "reauthTitle" to "Checking — Reautenticação necessária",
            "reauthBody" to "Abra o aplicativo para entrar novamente.",
        ),
    ),
    "scheduledPause" to d(
        "buttonLabel" to "Pausa Programada",
        "title" to "Pausa Programada",
        "explanation" to "A pausa programada economiza bateria suspendendo completamente as atualizações automáticas do Checking durante o período especificado. Enquanto a pausa estiver ativa, o aplicativo não realiza nenhuma atividade — nem por proximidade de uma localização, nem pela verificação periódica — retomando as atividades automáticas somente ao fim do período.",
        "enable" to "Ativar pausa programada.",
        "from" to "De:",
        "to" to "Até:",
        "suspendSaturdays" to "Suspender aos sábados.",
        "suspendSundays" to "Suspender aos domingos.",
        "close" to "Fechar",
        "notificationPaused" to "Pausa programada ativa",
    ),
    "instructions" to d(
        "heading" to "Instruções",
        "intro" to "Este guia mostra, passo a passo, como usar o Checking: registrar presença manualmente, ativar o Modo Automático (check-in e check-out por localização) e configurar a Pausa Programada.",
        "step1" to d(
            "title" to "1. Entrar no aplicativo",
            "item1" to "Na tela inicial, digite sua chave de 4 caracteres no campo 'Chave'. O campo fica laranja quando a chave é encontrada.",
            "item2" to "Digite sua senha no campo 'Senha'. Ao autenticar, os campos ficam verdes e aparece 'Autenticação concluída'.",
            "item3" to "Se você ainda não tem senha, o app abre o cadastro de senha automaticamente; se a chave não existir, ele oferece o autocadastro.",
        ),
        "step2" to d(
            "title" to "2. Registrar presença manualmente",
            "item1" to "Escolha 'Check-In' ou 'Check-Out' e o tipo 'Normal' ou 'Retroativo'.",
            "item2" to "Com o Modo Automático desligado, selecione o 'Local' na lista e toque em 'Registrar Check-In' (ou 'Check-Out').",
            "item3" to "O cartão no topo mostra seu último check-in e check-out; toque nele para ver a lista completa com data, hora e local.",
        ),
        "step3" to d(
            "title" to "3. Ativar o Modo Automático",
            "lead" to "Com o Modo Automático, o app faz check-in e check-out sozinho, com base na sua localização — ao entrar ou sair de uma área cadastrada, ao trazer o app para primeiro plano e em verificações periódicas.",
            "item1" to "Toque na engrenagem (ao lado dos campos de chave/senha) para abrir 'Ajustes'.",
            "item2" to "Toque em 'Atividades Automáticas' e marque a caixa 'Habilitar Atividades Automáticas'.",
            "item3" to "Conceda cada permissão da lista que aparece, tocando nela: Notificações, Localização 'o tempo todo' (Permitir sempre), Bateria sem restrição e — em alguns aparelhos — 'Iniciar com o aparelho'.",
            "item4" to "Quando a engrenagem fica com brilho VERDE, o Modo Automático está ativo e saudável. Brilho LARANJA indica que falta uma permissão recomendada.",
            "callout" to "Importante: para funcionar de forma confiável em segundo plano, conceda a Localização como 'Permitir o tempo todo' e desative a otimização de bateria para o Checking.",
        ),
        "step4" to d(
            "title" to "4. Ativar a Pausa Programada",
            "lead" to "A Pausa Programada economiza bateria suspendendo as atividades automáticas durante um período (por exemplo, à noite).",
            "item1" to "Em 'Ajustes', toque em 'Pausa Programada'.",
            "item2" to "Ative a opção e defina os horários 'De' e 'Até' (por exemplo, das 22:00 às 06:00).",
            "item3" to "Se quiser, marque também 'Suspender aos sábados' e/ou 'Suspender aos domingos'.",
            "item4" to "Durante a pausa, o app não realiza nenhuma atividade automática; ele retoma sozinho ao fim do período.",
        ),
        "step5" to d(
            "title" to "5. Acompanhar o histórico",
            "item1" to "Toque em 'ÚLTIMO CHECK-IN' ou 'ÚLTIMO CHECK-OUT' para abrir a tabela com Data, Hora e Local de cada registro.",
            "item2" to "Registros feitos próximos, porém fora de uma área cadastrada, aparecem como 'Localização não Cadastrada'.",
            "item3" to "Mesmo sem internet, seus registros ficam salvos no aparelho e são enviados assim que a conexão volta, sempre com o horário original.",
        ),
        "step6" to d(
            "title" to "6. Solicitar transporte",
            "item1" to "Toque em 'Transporte' para abrir o módulo de transporte de pessoal.",
            "item2" to "Informe o endereço e o horário desejados e envie a solicitação.",
            "item3" to "O responsável pela logística organiza as viagens; um motor de inteligência artificial sugere como agrupar passageiros e ordenar as paradas.",
        ),
        "step7" to d(
            "title" to "7. Em caso de acidente",
            "lead" to "O Modo Acidente é um recurso de segurança. Use-o apenas em uma emergência real.",
            "item1" to "Qualquer usuário pode abrir o Modo Acidente; isso avisa, em tempo real, todos os usuários do mesmo projeto.",
            "item2" to "Informe sua situação e zona: 'em segurança', 'no local do acidente, mas em segurança' ou 'no local do acidente e precisando de ajuda'.",
            "item3" to "Se possível, grave um vídeo do local: ele é enviado em tempo real para o painel do administrador.",
            "item4" to "O botão 'Acionar Serviço de Emergência' liga para o serviço de emergência local, informando o acidente e o local no idioma da região.",
        ),
        "step8" to d(
            "title" to "8. Outros ajustes",
            "item1" to "'Avisos': escolha quais notificações receber (atividades, pausa programada, acidente).",
            "item2" to "'Idioma': troque o idioma do aplicativo.",
            "item3" to "'Alterar Senha': defina uma nova senha quando precisar.",
            "item4" to "'Suporte': fale direto com a equipe pelo WhatsApp.",
            "item5" to "'Sobre': conheça a história do Checking e as partes que compõem o sistema.",
        ),
        "closing" to "Pronto! Com o Modo Automático ativo, você não precisa registrar nada manualmente — o Checking cuida disso por você.",
    ),
    "about" to d(
        "heading" to "Sobre o Checking",
        "introTitle" to "Como nasceu o Checking",
        "introBody" to "O Checking começou a ser desenvolvido em março de 2026, a partir da idealização do Engenheiro Dilnei Schmidt.\n" +
            "\n" +
            "Havia a necessidade de identificar rapidamente todos os funcionários da Petrobras presentes no local onde os trabalhos de construção e montagem aconteciam, caso ocorresse um acidente.\n" +
            "\n" +
            "A primeira solução do corpo gerencial de SMS foi um formulário online, preenchido por cada funcionário na chegada e na saída do local de trabalho. Funcionava para identificar quem estava presente, mas era trabalhoso e muitos esqueciam de preencher.\n" +
            "\n" +
            "Para aumentar a eficiência, Dilnei desenvolveu um aplicativo capaz de:\n" +
            "• identificar, por GPS, a proximidade do usuário com o local de trabalho e avisá-lo da necessidade de check-in;\n" +
            "• pré-ajustar alarmes em horários típicos de check-in e check-out, lembrando o usuário de preencher o formulário;\n" +
            "• preencher automaticamente o formulário com os dados do usuário e enviá-lo online.\n" +
            "\n" +
            "Isso facilitou as atividades e aumentou a frequência de preenchimento.\n" +
            "\n" +
            "Ainda em março de 2026, o Engenheiro Tamer Salmem conheceu as soluções implementadas e avançou no uso das tecnologias atuais de programação, desenvolvendo o sistema inicialmente idealizado por Dilnei.\n" +
            "\n" +
            "A intenção era que o usuário não precisasse se preocupar em abrir um aplicativo para fazer check-in ou check-out. Além disso, criar um controle em tempo real para que os administradores soubessem não apenas quem estava no trabalho, mas em qual das localizações cadastradas de cada projeto cada usuário estava — aumentando a capacidade de resposta em emergências.\n" +
            "\n" +
            "Assim, o sistema ganhou:\n" +
            "• ativação de serviços por geofencing (conforme a proximidade do usuário com o local de trabalho);\n" +
            "• execução de tarefas em segundo plano — check-in a cada mudança de localização dentro das instalações e check-out ao se afastar, sem o usuário sequer desbloquear o aparelho;\n" +
            "• envio em tempo real da localização dos usuários para o painel do administrador;\n" +
            "• possibilidade de cadastrar quantos projetos forem necessários, em qualquer lugar do mundo.\n" +
            "\n" +
            "O sistema também pode entrar no 'Modo Acidente'. Em caso de acidente, qualquer usuário pode disparar um alarme que avisa, em tempo real, todos os usuários do mesmo projeto. Com o Modo Acidente ativo:\n" +
            "• uma tabela é criada no painel do administrador, listando a situação de cada usuário: 'em segurança', 'no local do acidente, mas em segurança' e 'no local do acidente e precisando de ajuda';\n" +
            "• o usuário pode gravar um vídeo e enviá-lo em tempo real, como link na tabela, para o administrador ver cenas do local;\n" +
            "• o botão 'Acionar Serviço de Emergência' liga para o serviço de emergência local cadastrado, informando o acidente, o local e o contato do responsável, falando no idioma da região.\n" +
            "\n" +
            "A robustez e a confiabilidade do sistema trouxeram segurança operacional e resposta imediata para a equipe de SMS da Petrobras.\n" +
            "\n" +
            "Por fim, o Engenheiro Thiago Soares do Nascimento integrou as informações geradas pelo sistema aos dashboards gerenciais existentes, de modo que o novo sistema atue junto com o antigo preenchimento de formulários, mantendo os controles gerenciais atualizados.\n" +
            "\n" +
            "Assim nasceu o CHECKING.",
        "partsTitle" to "As partes do sistema",
        "partsIntro" to "O Checking é um sistema de controle de presença que registra a entrada e a saída de colaboradores nos locais de trabalho. Funciona por diferentes canais — leitores de cartão RFID instalados no local, um aplicativo Android, uma página web acessível pelo celular e um painel de administração — reunindo tudo em um só lugar.\n" +
            "\n" +
            "O conjunto é formado por:\n" +
            "• uma API, desenvolvida em Python/FastAPI;\n" +
            "• um website para os administradores do sistema;\n" +
            "• uma aplicação Web, responsiva para celulares e desktops;\n" +
            "• um dashboard para controle de transporte de pessoal;\n" +
            "• um aplicativo exclusivo para Android, desenvolvido em Kotlin.",
        "partApiTitle" to "API",
        "partApiBody" to "A API é o cérebro do sistema. Sempre que alguém faz check-in ou check-out — pelo leitor físico, pelo aplicativo ou pela página web — é ela que recebe a informação, verifica se está correta, salva no banco de dados e avisa os demais componentes em tempo real.\n" +
            "\n" +
            "Ela também preenche automaticamente o formulário corporativo no Microsoft Forms após cada registro, coordena o sistema de transporte, dispara alertas de emergência em caso de acidente e garante que nenhum dado se perca quando há instabilidade de conexão.",
        "partWebsiteTitle" to "Website",
        "partWebsiteBody" to "O website é o painel dos administradores. Por ele é possível ver em tempo real quem está em check-in e quem está em check-out, além de gerenciar todo o sistema sem conhecimento técnico.\n" +
            "\n" +
            "Principais funções: cadastrar e editar colaboradores, criar projetos e suas regras, definir as áreas geográficas reconhecidas, consultar relatórios de presença e exportar dados. É também o ponto central para acionar e acompanhar o Modo Acidente — vendo a situação de cada colaborador em tempo real e coordenando a resposta de emergência.",
        "partWebappTitle" to "Aplicação Web",
        "partWebappBody" to "A aplicação web é a ferramenta dos colaboradores. Funciona no navegador do celular ou do computador, sem instalar nada, e permite registrar entrada e saída, consultar o histórico e solicitar transporte.\n" +
            "\n" +
            "Ao ativar as Atividades Automáticas, o próprio celular detecta a localização e faz check-in ou check-out automaticamente ao entrar ou sair das áreas cadastradas. Em caso de acidente, a interface muda e passa a pedir que o colaborador informe sua situação e zona de segurança.\n" +
            "\n" +
            "Está disponível em seis idiomas (português, inglês, chinês, malaio, indonésio e tagalo) para atender equipes internacionais.",
        "partTransportTitle" to "Dashboard de Transportes",
        "partTransportBody" to "O dashboard de transportes é a ferramenta do responsável pela logística de deslocamento. Por ele é possível cadastrar os veículos, visualizar e organizar as solicitações de transporte dos colaboradores e alocar cada pessoa em um veículo para o dia.\n" +
            "\n" +
            "Conta com um motor de inteligência artificial que analisa endereços e horários e sugere automaticamente como agrupar passageiros e ordenar as paradas de forma otimizada — reduzindo o tempo de deslocamento e o número de viagens. O responsável pode aceitar a sugestão, ajustá-la ou montar a alocação manualmente.",
        "partAndroidTitle" to "Aplicativo Android",
        "partAndroidBody" to "O aplicativo Android oferece as mesmas funções da aplicação web, com uma experiência mais completa no dia a dia. A principal vantagem é a automação por geolocalização: o app roda em segundo plano e registra check-in ou check-out automaticamente conforme o colaborador entra e sai das áreas cadastradas, sem depender do navegador.\n" +
            "\n" +
            "Ele também funciona sem internet: sem conexão, os registros ficam salvos no celular e são enviados assim que a conexão volta, sempre com o horário original. Inclui o histórico com data, hora e local de cada evento, o módulo de transporte e o modo de emergência para acidentes.",
        "rulesTitle" to "Situações de check-in e check-out",
        "rulesIntro" to "As situações abaixo descrevem, passo a passo, o que o sistema deve fazer para cada usuário (check-in ou check-out) em cada cenário típico. A Aplicação Web e o Aplicativo Nativo seguem as regras de seus respectivos blocos.",
        "rulesWebTitle" to "Situações — Aplicação Web",
        "rulesWebBody" to "## Situação 1 — Check-out por afastamento\n" +
            "• Atividades Automáticas habilitadas, com permissão total de localização.\n" +
            "• A última atividade foi um check-in.\n" +
            "• O app atualiza a localização e percebe que o usuário está na 'Zona de CheckOut' ou a mais de 2 km de qualquer local cadastrado (exceto a Zona de CheckOut).\n" +
            "• Como a última atividade foi um check-in, o app realiza o check-out.\n" +
            "\n" +
            "## Situação 2 — Já em check-out, longe ou na Zona de CheckOut\n" +
            "• A última atividade foi um check-out.\n" +
            "• O usuário está na 'Zona de CheckOut' ou a mais de 2 km de qualquer local cadastrado.\n" +
            "• Nenhuma ação: o check-out não se repete por mudança de localização.\n" +
            "\n" +
            "## Situação 3 — Chegada ao trabalho (check-in)\n" +
            "• A última atividade foi um check-out.\n" +
            "• O usuário está DENTRO de uma área cadastrada diferente da 'Zona de CheckOut' (correspondência efetiva com a área, não apenas proximidade).\n" +
            "• O usuário está efetivamente no trabalho (inclusive no primeiro check-in do dia).\n" +
            "• O app realiza o check-in e atualiza a localização para a área cadastrada correspondente.\n" +
            "! IMPORTANTE: se o usuário NÃO estiver dentro de nenhuma área cadastrada — ainda que próximo (menos de 2 km de alguma coordenada, fora a Zona de CheckOut) —, o app NÃO faz check-in automático; apenas exibe 'Localização não Cadastrada' (igual à Situação 5).\n" +
            "\n" +
            "## Situação 4 — Novo check-in (sempre)\n" +
            "• A última atividade foi um check-in.\n" +
            "• O usuário está em uma área cadastrada diferente da 'Zona de CheckOut'.\n" +
            "• O app realiza um novo check-in INDEPENDENTEMENTE de a localização ter mudado.\n" +
            "• Mesmo no MESMO local do último check-in, um novo check-in é feito para registrar/atualizar a localização e o horário.\n" +
            "\n" +
            "## Situação 5 — Próximo, porém fora de área\n" +
            "• A última atividade foi um check-in.\n" +
            "• O usuário não está em nenhuma área cadastrada, mas também não está a mais de 2 km de alguma coordenada cadastrada (fora a Zona de CheckOut). Ou seja, está próximo do trabalho.\n" +
            "• Nenhuma ação: o app apenas exibe 'Localização não Cadastrada'.\n" +
            "\n" +
            "## Situação 6 — Botão 'Atualizar' após check-in\n" +
            "• O app já está em primeiro plano; a última atividade foi um check-in.\n" +
            "• O usuário toca em 'Atualizar' para atualizar a localização.\n" +
            "• O app realiza um novo check-in INDEPENDENTEMENTE de a localização ter mudado, para registrar/atualizar a localização e o horário.\n" +
            "\n" +
            "## Situação 7 — Saída da Zona de CheckOut\n" +
            "• Em primeiro plano; a última atividade foi um check-out; o usuário está na 'Zona de CheckOut' (nenhuma ação).\n" +
            "• O usuário toca em 'Atualizar' e o app percebe que ele saiu da Zona de CheckOut, para:\n" +
            "• Variante 7A — uma área cadastrada diferente da 'Zona de CheckOut';\n" +
            "• Variante 7B — nenhuma área cadastrada, mas ainda próximo (menos de 2 km, fora a Zona de CheckOut).\n" +
            "• Em ambas, o app realiza imediatamente um check-in, atualizando a localização para a área cadastrada ou, sem correspondência exata, para 'Localização não Cadastrada'.\n" +
            "\n" +
            "## Situação 8 — Zona Mista\n" +
            "• O app identifica que a posição corresponde à 'Zona Mista' (na primeira entrada ou em leitura consecutiva).\n" +
            "• Se a última atividade relevante NÃO foi na própria 'Zona Mista', a alternância é imediata: 8A — após check-in → check-out na 'Zona Mista'; 8B — após check-out → check-in na 'Zona Mista'.\n" +
            "• O campo 'Intervalo de Tempo para Zona Mista' (aba 'Cadastro' do website) é o cooldown só para leituras consecutivas na própria Zona Mista: enquanto tempo_decorrido < intervalo, bloqueia nova alternância; quando >= intervalo, volta a permitir.\n" +
            "• Exceção após check-in na Zona Mista: ir para a 'Zona de CheckOut' ou além da 'Distância mínima para check-out automático' → check-out imediato, sem aguardar o cooldown.\n" +
            "• Exceção após check-out na Zona Mista: ir para outra área cadastrada (exceto Zona de CheckOut e Zona Mista) ou continuar dentro da distância mínima → check-in imediato, descartando o cooldown.\n" +
            "\n" +
            "## Situação 9 — Modo manual (Atividades Automáticas desligadas)\n" +
            "• O usuário está autenticado; as Atividades Automáticas estão DESABILITADAS.\n" +
            "• O app atualiza a localização se houver permissão; senão, exibe 'Permissão negada'.\n" +
            "• O usuário escolhe 'check-in' ou 'check-out', 'Normal' ou 'Retroativo', seleciona o 'Local' (disponível sempre que as Atividades Automáticas estão desligadas) e toca em 'Registrar'.\n" +
            "• O app segue o fluxo normal e realiza a atividade conforme as seleções.",
        "rulesNativeTitle" to "Situações — Aplicativo Nativo (Android)",
        "rulesNativeBody" to "## Situação 1 — Check-out por afastamento\n" +
            "• A última atividade foi um check-in.\n" +
            "• O usuário está na 'Zona de CheckOut' ou a mais de 2 km de qualquer local cadastrado (exceto a Zona de CheckOut).\n" +
            "• O app realiza o check-out.\n" +
            "\n" +
            "## Situação 2 — Já em check-out, longe ou na Zona de CheckOut\n" +
            "• A última atividade foi um check-out.\n" +
            "• O usuário está na 'Zona de CheckOut' ou a mais de 2 km de qualquer local cadastrado.\n" +
            "• Nenhuma ação: o check-out não se repete por mudança de localização.\n" +
            "\n" +
            "## Situação 3 — Chegada ao trabalho (check-in)\n" +
            "• A última atividade foi um check-out.\n" +
            "• O usuário está DENTRO de uma área cadastrada diferente da 'Zona de CheckOut' (correspondência efetiva, não apenas proximidade).\n" +
            "• O app realiza o check-in e atualiza a localização para a área correspondente.\n" +
            "! IMPORTANTE: partindo de um CHECK-OUT, se o usuário NÃO estiver dentro de nenhuma área cadastrada — ainda que próximo —, o app NÃO faz check-in; apenas exibe 'Localização não Cadastrada' (ver Variante 7B). Quando a última atividade foi um CHECK-IN e o usuário fica próximo, porém fora de área, o comportamento é diferente: o app faz um check-in com 'Localização não Cadastrada' como mudança (ver Situação 5).\n" +
            "\n" +
            "## Situação 4 — Novo check-in apenas por mudança de local\n" +
            "• A última atividade foi um check-in.\n" +
            "• O usuário está em uma área cadastrada diferente da 'Zona de CheckOut'.\n" +
            "• O app realiza um novo check-in APENAS se a área for DIFERENTE da do último check-in.\n" +
            "• No MESMO local do último check-in, NENHUMA ação (isto elimina o check-in duplicado). Ao mudar de área, o novo check-in registra/atualiza a localização e o horário.\n" +
            "\n" +
            "## Situação 5 — Próximo, porém fora de área (continuação)\n" +
            "• A última atividade foi um check-in.\n" +
            "• O usuário não está em nenhuma área cadastrada, mas está próximo (menos de 2 km de alguma coordenada, fora a Zona de CheckOut).\n" +
            "• Como saiu da área, o app faz um check-in com 'Localização não Cadastrada', registrando a continuidade do deslocamento.\n" +
            "• Só ocorre como MUDANÇA: se o último check-in já foi 'Localização não Cadastrada', NENHUMA ação (não se repete).\n" +
            "\n" +
            "## Situação 6 — Botão 'Atualizar' após check-in\n" +
            "• Em primeiro plano; a última atividade foi um check-in.\n" +
            "• O usuário toca em 'Atualizar'.\n" +
            "• Novo check-in APENAS se a localização for DIFERENTE da do último check-in (mesma regra da Situação 4). No MESMO local, NENHUMA ação.\n" +
            "\n" +
            "## Situação 7 — Saída da Zona de CheckOut\n" +
            "• Em primeiro plano; a última atividade foi um check-out; o usuário está na 'Zona de CheckOut' (nenhuma ação).\n" +
            "• O usuário toca em 'Atualizar' e o app atualiza a localização para: Variante 7A — uma área cadastrada diferente da 'Zona de CheckOut'; Variante 7B — nenhuma área cadastrada, mas ainda próximo (menos de 2 km, fora a Zona de CheckOut).\n" +
            "• 7A: como a última atividade foi check-out, o app faz o check-in imediatamente na área correspondente.\n" +
            "• 7B: como o usuário está em check-out e NÃO está dentro de nenhuma área, o app NÃO faz check-in; apenas exibe 'Localização não Cadastrada' (mesma regra da nota da Situação 3).\n" +
            "\n" +
            "## Situação 8 — Zona Mista\n" +
            "• O app identifica a 'Zona Mista' e, se a última atividade relevante não foi nela, alterna imediatamente: 8A — após check-in → check-out na 'Zona Mista'; 8B — após check-out → check-in na 'Zona Mista'.\n" +
            "• O 'Intervalo de Tempo para Zona Mista' é o cooldown só para leituras consecutivas na própria Zona Mista: enquanto tempo_decorrido < intervalo, bloqueia nova alternância; quando >= intervalo, volta a permitir.\n" +
            "• Exceções imediatas (descartando o cooldown): ir para a 'Zona de CheckOut' ou além da distância mínima → check-out; ir para outra área cadastrada ou continuar dentro da distância mínima → check-in.\n" +
            "\n" +
            "## Situação 9 — Modo manual (Atividades Automáticas desligadas)\n" +
            "• Usuário autenticado; Atividades Automáticas DESABILITADAS.\n" +
            "• O app atualiza a localização se houver permissão; senão, exibe 'Permissão negada'.\n" +
            "• O usuário escolhe check-in/check-out, Normal/Retroativo, seleciona o 'Local' e toca em 'Registrar'.\n" +
            "• O app segue o fluxo normal conforme as seleções.",
        "notesTitle" to "Observações gerais",
        "notesBody" to "## Gatilho de primeiro plano\n" +
            "• Abrir o app ou trazê-lo para primeiro plano, com Atividades Automáticas habilitadas e usuário autenticado, dispara a avaliação automática (o motor decide check-in ou check-out conforme as situações). O mesmo vale para o geofencing e para a verificação periódica a cada 15 minutos.\n" +
            "• Não há check-in periódico 'às cegas': a verificação de 15 em 15 minutos sempre confere a localização e mantém o 'pular se nada mudou'.\n" +
            "\n" +
            "## Check-in só por mudança de localização\n" +
            "• O check-in automático só ocorre quando a localização resolvida é DIFERENTE da do último check-in. Mesma localização → nenhuma ação. Essa regra (Situações 4 e 6) é o que ELIMINA o check-in duplicado.\n" +
            "\n" +
            "## FORMS por projeto\n" +
            "• No primeiro check-in do dia e em cada check-out, o formulário é preenchido e enviado UMA VEZ POR PROJETO em que o usuário está cadastrado (respeitando o 'forms habilitado' de cada projeto). Ex.: usuário nos projetos P80 e P83 → duas submissões. Usuário de projeto único → uma submissão.\n" +
            "\n" +
            "## Invariantes de check-out (preservadas)\n" +
            "• O check-out automático ocorre em todos os casos descritos (Zona de CheckOut, distância além do limite, alternância da Zona Mista); nunca há dois check-outs consecutivos; após um check-out, a próxima atividade automática é sempre um check-in.",
    ),
)

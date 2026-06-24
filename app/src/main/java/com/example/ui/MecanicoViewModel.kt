package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN, HOME, MANUALS, FORUM, TUTORIALS, PROFILE, AI_CHAT
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class VideoTutorial(
    val id: Int,
    val title: String,
    val duration: String,
    val category: String,
    val author: String,
    val description: String,
    val videoUrl: String = ""
)

class MecanicoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MecanicoRepository(db)

    // --- Configurations & Theme Settings ---
    val isDarkMode = MutableStateFlow(true) // Default to customizable Dark Mode
    val activeLanguage = MutableStateFlow("PT-BR") // Default to PT-BR
    val activeScreen = MutableStateFlow(Screen.LOGIN)

    // --- Search & Filters State ---
    val searchQuery = MutableStateFlow("")
    val filterVehicleType = MutableStateFlow("") // empty means All
    val filterBrand = MutableStateFlow("")
    val filterYear = MutableStateFlow(0)
    val filterCategory = MutableStateFlow("") // empty means All

    // --- UI Selection States ---
    val selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedTopic = MutableStateFlow<ForumTopic?>(null)

    // --- User Profile Details ---
    val userName = MutableStateFlow("Jonathas Santos")
    val userRole = MutableStateFlow("Mecânico de Veículos")
    val userWorkshop = MutableStateFlow("Santos Auto Center - SP")

    // --- Gemini AI Assistant Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Olá! Sou o Assistente Técnico IA do AutoPedia. Como posso ajudar com sua dúvida de oficina hoje?\n\nExemplo: 'Como testar sensor NoX do Accelo?' ou 'O que causa barulho de ferro batendo no Gol G5?'",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    val isChatLoading = MutableStateFlow(false)

    // --- Video Tutorials Source ---
    val videoTutorials = listOf(
        VideoTutorial(
            id = 1,
            title = "Diagnóstico do Motor 1.0 3 Cilindros Chevrolet Onix",
            duration = "18:25",
            category = "Mecânica",
            author = "Dr. Carro",
            description = "Passo a passo detalhado sobre como verificar a pressão de óleo no motor de 3 cilindros turbo, além de como diagnosticar o desgaste prematuro da correia banhada a óleo."
        ),
        VideoTutorial(
            id = 2,
            title = "Esquema do Chicote do Acelerador do Voyage/Gol",
            duration = "12:40",
            category = "Elétrica",
            author = "Carlos_Eletro",
            description = "Como medir a continuidade da pinagem do pedal de aceleração eletrônica até a central ECU Magneti Marelli, evitando a troca errônea do corpo de borboletas (TBI)."
        ),
        VideoTutorial(
            id = 3,
            title = "Técnicas de Retoque Invisível em Cores Metálicas",
            duration = "22:10",
            category = "Pintura",
            author = "Mestre_Pintor",
            description = "O segredo para fazer o retoque de para-lama em carros de cor Prata Metálica. Aprenda a aplicar o diluente de retoque (blender) para sumir com a emenda do verniz."
        ),
        VideoTutorial(
            id = 4,
            title = "Troca do Tensionador CG 160 Passo a Passo",
            duration = "08:15",
            category = "Mecânica",
            author = "Canal Moto Mecânica",
            description = "Veja como substituir o tensionador da corrente de comando da Titan 160 que apresenta ruído metálico forte, sem a necessidade de remover o cilindro."
        ),
        VideoTutorial(
            id = 5,
            title = "Eliminando Falhas de NoX e Arla 32 no Accelo 1016",
            duration = "15:45",
            category = "Caminhão",
            author = "Pé na Estrada",
            description = "Instruções práticas para realizar a limpeza da válvula dosadora de ureia no escapamento e verificar a fiação CAN do sensor NoX Mercedes-Benz."
        )
    )

    // --- Reactive Data Flows from Room ---
    val vehicles: StateFlow<List<Vehicle>> = combine(
        searchQuery,
        filterBrand,
        filterYear,
        filterVehicleType
    ) { query, brand, year, type ->
        val y = if (year == 0) null else year
        val defaultVehicles = repository.searchVehicles(query, if (brand.isEmpty()) null else brand, y, if (type.isEmpty()) null else type).first()
        
        if (!query.isNullOrBlank()) {
            val matchedParts = repository.searchParts(query, null).first()
            if (matchedParts.isNotEmpty()) {
                val vehicleIds = matchedParts.map { it.vehicleId }.distinct()
                val extraVehicles = repository.getAllVehicles().first().filter { it.id in vehicleIds }
                (defaultVehicles + extraVehicles).distinctBy { it.id }
            } else {
                defaultVehicles
            }
        } else {
            defaultVehicles
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContributions: StateFlow<List<UserContribution>> = repository.getAllContributions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forumTopics: StateFlow<List<ForumTopic>> = repository.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTopicReplies: StateFlow<List<ForumReply>> = selectedTopic
        .flatMapLatest { topic ->
            if (topic != null) repository.getRepliesForTopic(topic.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedItems: StateFlow<List<SavedItem>> = repository.getAllSavedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<UserBadge>> = repository.getAllBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<InAppNotification>> = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.getUnreadNotificationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wikiSections = MutableStateFlow<Map<Int, List<WikiSection>>>(emptyMap())

    init {
        // Automatically seed database on initialization
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        initializeWikiSections()
    }

    // --- Actions & Methods ---

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun toggleLanguage() {
        activeLanguage.value = if (activeLanguage.value == "PT-BR") "EN" else "PT-BR"
    }

    fun changeScreen(screen: Screen) {
        activeScreen.value = screen
    }

    // Vehicle & Parts Queries
    fun getPartsForVehicle(vehicleId: Int): Flow<List<PartAndDefect>> =
        repository.getPartsForVehicle(vehicleId)

    // Contribution Actions
    fun addUserContribution(vehicleId: Int?, title: String, body: String) {
        viewModelScope.launch {
            val contribution = UserContribution(
                vehicleId = vehicleId,
                authorName = userName.value,
                title = title,
                body = body
            )
            repository.insertContribution(contribution)
        }
    }

    // Forum Actions
    fun createForumTopic(title: String, body: String, category: String) {
        viewModelScope.launch {
            val topic = ForumTopic(
                title = title,
                author = userName.value,
                body = body,
                category = category
            )
            repository.insertTopic(topic)
        }
    }

    fun addForumReply(topicId: Int, body: String) {
        viewModelScope.launch {
            val reply = ForumReply(
                topicId = topicId,
                author = userName.value,
                body = body
            )
            repository.insertReply(reply)
        }
    }

    fun toggleFollowTopic(topic: ForumTopic) {
        viewModelScope.launch {
            val updated = topic.copy(isFollowed = !topic.isFollowed)
            repository.updateTopic(updated)
            selectedTopic.value = updated

            val msg = if (updated.isFollowed) "Seguindo" else "Deixou de seguir"
            repository.insertNotification(
                title = "Fórum Atualizado",
                body = "Você agora está $msg o tópico '${topic.title}'."
            )
        }
    }

    // Offline / Save Actions
    fun isSaved(type: String, refId: Int): Flow<Boolean> = repository.isItemSaved(type, refId)

    fun toggleSaveItem(type: String, refId: Int, title: String, description: String) {
        viewModelScope.launch {
            val saved = repository.isItemSaved(type, refId).first()
            if (saved) {
                repository.removeSavedItem(type, refId)
            } else {
                repository.saveItem(type, refId, title, description)
            }
        }
    }

    // Notifications Action
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    // AI Mechanic Assistant Chat Action
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return

        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            isChatLoading.value = true

            // Set a diagnostic system instruction for the AI model
            val systemInstruction = """
                Você é o Doutor Mecânico, o assistente técnico especialista de inteligência artificial da plataforma AutoPedia.
                Sua função é auxiliar mecânicos de automóveis, motocicletas e caminhões, além de eletricistas e pintores de veículos no Brasil.
                Responda em português (ou inglês se as perguntas forem em inglês), forneça diagnósticos técnicos precisos, códigos de peças se souber, procedimentos passo a passo, defeitos crônicos conhecidos de veículos populares no Brasil (Gol, Onix, Uno, Palio, Ka, HB20, CG 150/160, Mercedes Accelo/Constellation, etc.) e esquemas elétricos conceituais.
                Seja prático, use vocabulário técnico de oficina e ofereça dicas de segurança.
            """.trimIndent()

            val aiResponse = GeminiClient.askGemini(text, systemInstruction)
            val aiMsg = ChatMessage(text = aiResponse, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg

            isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Chat reiniciado. Como posso ajudar você na oficina agora?",
                isUser = false
            )
        )
    }

    fun toggleUserRole() {
        if (userRole.value == "Mecânico de Veículos" || userRole.value == "Profissional") {
            userRole.value = "Dono de Veículo"
            userWorkshop.value = "Visualizador Comum"
        } else {
            userRole.value = "Profissional"
            userWorkshop.value = "Santos Auto Center - SP"
        }
    }

    fun suggestWikiEdit(vehicleId: Int, sectionId: Int, content: String) {
        val currentMap = wikiSections.value
        val sections = currentMap[vehicleId] ?: emptyList()
        val updatedSections = sections.map { sec ->
            if (sec.id == sectionId) {
                val newProposal = WikiProposal(
                    id = (sec.pendingProposals.maxOfOrNull { it.id } ?: 0) + 1,
                    author = userName.value,
                    content = content,
                    status = "PENDING",
                    timestamp = "Agora"
                )
                sec.copy(pendingProposals = sec.pendingProposals + newProposal)
            } else sec
        }
        wikiSections.value = currentMap + (vehicleId to updatedSections)
    }

    fun approveWikiProposal(vehicleId: Int, sectionId: Int, proposalId: Int) {
        val currentMap = wikiSections.value
        val sections = currentMap[vehicleId] ?: emptyList()
        val updatedSections = sections.map { sec ->
            if (sec.id == sectionId) {
                val proposal = sec.pendingProposals.find { it.id == proposalId }
                if (proposal != null) {
                    val updatedProposals = sec.pendingProposals.map { prop ->
                        if (prop.id == proposalId) prop.copy(status = "APPROVED") else prop
                    }
                    sec.copy(
                        content = proposal.content,
                        lastUpdatedBy = proposal.author,
                        lastUpdatedDaysAgo = 0,
                        pendingProposals = updatedProposals
                    )
                } else sec
            } else sec
        }
        wikiSections.value = currentMap + (vehicleId to updatedSections)
    }

    fun rejectWikiProposal(vehicleId: Int, sectionId: Int, proposalId: Int) {
        val currentMap = wikiSections.value
        val sections = currentMap[vehicleId] ?: emptyList()
        val updatedSections = sections.map { sec ->
            if (sec.id == sectionId) {
                val updatedProposals = sec.pendingProposals.map { prop ->
                    if (prop.id == proposalId) prop.copy(status = "REJECTED") else prop
                }
                sec.copy(pendingProposals = updatedProposals)
            } else sec
        }
        wikiSections.value = currentMap + (vehicleId to updatedSections)
    }

    fun addWikiComment(vehicleId: Int, sectionId: Int, commentText: String) {
        val currentMap = wikiSections.value
        val sections = currentMap[vehicleId] ?: emptyList()
        val updatedSections = sections.map { sec ->
            if (sec.id == sectionId) {
                val newComment = WikiComment(
                    author = userName.value,
                    text = commentText,
                    timestamp = "Agora"
                )
                sec.copy(comments = sec.comments + newComment)
            } else sec
        }
        wikiSections.value = currentMap + (vehicleId to updatedSections)
    }

    private fun initializeWikiSections() {
        val map = mapOf(
            1 to listOf(
                WikiSection(
                    id = 1,
                    vehicleId = 1,
                    title = "Troca da Correia de Acessórios Banhada em Óleo (Ecotec 1.0)",
                    content = "Procedimento crítico de manutenção. A correia banhada em óleo exige óleo lubrificante homologado com especificação correta (Dexos 1 Gen 2). Utilizar ferramentas de sincronismo para travar os comandos e a polia do virabrequim. Troca recomendada a cada 240.000 km ou 15 anos sob uso normal, mas em regime severo/urbano inspecionar a cada 40.000 km por sinais de descamação que possam obstruir o pescador de óleo.",
                    lastUpdatedBy = "Carlos_Eletro",
                    lastUpdatedDaysAgo = 12,
                    comments = listOf(
                        WikiComment("Mecanico_Senior", "Sempre troque o pescador ou limpe a peneira ao trocar essa correia!", "3 dias atrás")
                    )
                ),
                WikiSection(
                    id = 2,
                    vehicleId = 1,
                    title = "Reset do Módulo de Injeção Eletrônica",
                    content = "Caso ocorra erro fantasma de mistura, desligar os polos da bateria por 1 hora, curto-circuitar os cabos desconectados por 30 segundos (procedimento de descarga de capacitores). Reconectar e deixar o veículo em marcha lenta por 15 minutos até reaprender os parâmetros de A/F (Ar/Combustível).",
                    lastUpdatedBy = "Dr. Carro",
                    lastUpdatedDaysAgo = 25
                ),
                WikiSection(
                    id = 3,
                    vehicleId = 1,
                    title = "Pintura & Tratamento de Parachoques de Plástico",
                    content = "Como pintar parachoques de plástico do Onix: Realizar o lixamento inicial com lixa grão 400. Aplicar o selador para plástico (Promotor de Aderência) em duas demãos finas. Em seguida, aplicar primer PU e lixar com grão 600 ou 800 seco. Aplicar a tinta poliéster e finalizar com verniz alto sólidos de alta resistência contra pedriscos.",
                    lastUpdatedBy = "Mestre_Pintor",
                    lastUpdatedDaysAgo = 5
                )
            ),
            2 to listOf(
                WikiSection(
                    id = 1,
                    vehicleId = 2,
                    title = "Sincronismo do Motor Firefly 1.3 3 Cilindros",
                    content = "O motor Firefly utiliza uma corrente de sincronismo muito robusta. Entretanto, em caso de desmontagem, é obrigatório utilizar o conjunto de ferramentas de fasagem específico (Raven 141015) para travar o comando de válvulas único na traseira do cabeçote e alinhar a marcação da polia do virabrequim com o sensor de rotação. Torque do parafuso da polia do virabrequim é de 40 Nm + 90 graus.",
                    lastUpdatedBy = "Felipe_Strada",
                    lastUpdatedDaysAgo = 8
                ),
                WikiSection(
                    id = 2,
                    vehicleId = 2,
                    title = "Lubricação e Alinhamento de Feixe de Molas Traseiro",
                    content = "Se a traseira apresentar rangido característico ao carregar peso, limpe a sujeira acumulada entre as lâminas com água de alta pressão. Não aplique graxa comum pois atrai poeira e aumenta o desgaste. O correto é aplicar spray de silicone seco ou grafite em pó para eliminar o ruído. Verifique as buchas de poliuretano a cada 50.000 km.",
                    lastUpdatedBy = "Mecânico Geral",
                    lastUpdatedDaysAgo = 18
                ),
                WikiSection(
                    id = 3,
                    vehicleId = 2,
                    title = "Alinhamento e Ajuste de Portas e Caçamba",
                    content = "Para alinhar a tampa traseira da caçamba e as portas da cabine dupla: Soltar levemente os parafusos das dobradiças torx T40, ajustar a folga periférica utilizando calços de borracha de 4mm para manter o espaçamento simétrico. Apertar os parafusos alternadamente. Aplicar selante de poliuretano (KPO) nas emendas internas para evitar infiltração de água.",
                    lastUpdatedBy = "Mestre_Pintor",
                    lastUpdatedDaysAgo = 2
                )
            ),
            3 to listOf(
                WikiSection(
                    id = 1,
                    vehicleId = 3,
                    title = "Procedimento do Ponto do Motor AP 1.6",
                    content = "O clássico motor AP tem marcas de sincronismo simples mas críticas. Alinhar a marca 'OT' no volante do motor com o ponteiro na carcaça da embreagem. A polia do comando de válvulas deve ter a marca 'OT' traseira alinhada com a superfície do cabeçote. A polia intermediária deve alinhar o ponto com a marca na polia do virabrequim para manter o distribuidor de ignição apontado para o cilindro 1.",
                    lastUpdatedBy = "Gol_Quadrado_Fan",
                    lastUpdatedDaysAgo = 30
                ),
                WikiSection(
                    id = 2,
                    vehicleId = 3,
                    title = "Diagnóstico do Sensor de Fase EA111",
                    content = "Se a luz da injeção piscar e acusar código P0340, meça a alimentação de 5V no conector do sensor de fase. Use osciloscópio para ler o sinal de onda quadrada enquanto gira o motor. Se a onda estiver falhando ou com picos de ruído, substitua o sensor. O chicote elétrico perto do coletor de admissão costuma quebrar devido à vibração.",
                    lastUpdatedBy = "Carlos_Eletro",
                    lastUpdatedDaysAgo = 14
                ),
                WikiSection(
                    id = 3,
                    vehicleId = 3,
                    title = "Reparo de Trincas e Solda na Coluna B",
                    content = "Defeito comum em Gols rebaixados ou com muito tempo de uso severo. Realizar o lixamento total da chapa afetada. Soldar utilizando processo MIG com gás Argônio/CO2 para evitar enfraquecimento térmico da estrutura. Aplicar primer fosfatizante (Wash Primer) contra corrosão antes da massa poliéster e primer PU de enchimento.",
                    lastUpdatedBy = "Soldador_Pro",
                    lastUpdatedDaysAgo = 4
                )
            ),
            4 to listOf(
                WikiSection(
                    id = 1,
                    vehicleId = 4,
                    title = "Procedimento de Ajuste de Folga de Válvulas (Motor OHC)",
                    content = "Ajuste obrigatório com motor totalmente frio (abaixo de 35°C). Retirar a tampa de cabeçote, girar o motor até alinhar a marca 'T' do volante magnético com a marca do bloco. Ajustar a folga de admissão em 0.08 mm e de escape em 0.12 mm utilizando lâminas calibradoras de precisão. Apertar a contra-porca a 10 Nm.",
                    lastUpdatedBy = "Moto_Mec",
                    lastUpdatedDaysAgo = 9
                ),
                WikiSection(
                    id = 2,
                    vehicleId = 4,
                    title = "Limpeza de Bico Injetor de Combustível Keihin",
                    content = "Colocar o eletroinjetor na cuba de ultrassom com solução de limpeza por 15 minutos. Após o ciclo, montar na máquina de teste de vazão e verificar a estanqueidade sob pressão de 3.0 bar por 60 segundos. Vazão desigual indica bico danificado que deve ser substituído para evitar mistura pobre.",
                    lastUpdatedBy = "Carlos_Eletro",
                    lastUpdatedDaysAgo = 11
                ),
                WikiSection(
                    id = 3,
                    vehicleId = 4,
                    title = "Tratamento e Repintura do Tanque de Combustível",
                    content = "Tanques de moto sofrem corrosão interna por álcool. Efetuar limpeza química com desengraxante fosfatizante. Para a repintura externa: aplicar lixa 320, corrigir pequenos amassados com massa poliéster rápida, aplicar primer PU cinza, realizar a pintura em três demãos cruzadas e cobrir com verniz bicomponente PU automotivo resistente a respingos de gasolina.",
                    lastUpdatedBy = "Mestre_Pintor",
                    lastUpdatedDaysAgo = 6
                )
            ),
            5 to listOf(
                WikiSection(
                    id = 1,
                    vehicleId = 5,
                    title = "Procedimento de Limpeza do Filtro de ARLA 32",
                    content = "O sistema de pós-tratamento SCR exige cuidados extremos. A cada 120.000 km, remova o copo do filtro de ARLA, extraia o elemento filtrante. Se houver cristalização de ureia, faça a limpeza utilizando apenas água morna desmineralizada. Nunca utilize solventes de petróleo. Substitua o elemento por um original para garantir a perfeita pulverização do bico dosador.",
                    lastUpdatedBy = "Diesel_Tech",
                    lastUpdatedDaysAgo = 15
                ),
                WikiSection(
                    id = 2,
                    vehicleId = 5,
                    title = "Calibração Eletrônica da Transmissão V-Tronic ZF ASTronic",
                    content = "Conecte o scanner automotivo compatível na porta OBD. Acesse o módulo da transmissão, selecione 'Aprender ponto de contato da embreagem'. Ligue o motor, mantenha os balões de ar cheios acima de 8.5 bar, acione o freio de estacionamento e siga os comandos do scanner para acoplamento dinâmico. Corrige trancos nas saídas de marcha.",
                    lastUpdatedBy = "Carlos_Eletro",
                    lastUpdatedDaysAgo = 20
                ),
                WikiSection(
                    id = 3,
                    vehicleId = 5,
                    title = "Tratamento Anticorrosivo e Pintura de Chassi/Longarinas",
                    content = "Caminhões pesados sofrem alta oxidação salina e química. Escovar as longarinas com escova de aço rotativa para retirar ferrugem solta. Aplicar convertedor de ferrugem à base de ácido fosfórico. Em seguida, aplicar tinta fundo Epóxi Ricos em Zinco e finalizar com acabamento esmalte poliuretano preto chassi fosco de alta espessura.",
                    lastUpdatedBy = "Mestre_Pintor",
                    lastUpdatedDaysAgo = 7
                )
            )
        )
        wikiSections.value = map
    }
}

data class WikiSection(
    val id: Int,
    val vehicleId: Int,
    val title: String,
    val content: String,
    val lastUpdatedBy: String,
    val lastUpdatedDaysAgo: Int,
    val pendingProposals: List<WikiProposal> = emptyList(),
    val comments: List<WikiComment> = emptyList()
)

data class WikiProposal(
    val id: Int,
    val author: String,
    val content: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val timestamp: String
)

data class WikiComment(
    val author: String,
    val text: String,
    val timestamp: String
)

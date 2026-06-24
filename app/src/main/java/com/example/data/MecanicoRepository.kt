package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import io.github.jan.supabase.postgrest.postgrest
import java.io.IOException
import java.util.UUID

class MecanicoRepository(private val db: AppDatabase) {

    private val vehicleDao = db.vehicleDao()
    private val partDao = db.partDao()
    private val contributionDao = db.contributionDao()
    private val forumDao = db.forumDao()
    private val savedItemDao = db.savedItemDao()
    private val badgeDao = db.badgeDao()
    private val notificationDao = db.notificationDao()

    init {
        // Register deterministic UUIDs for standard static vehicles (IDs 1 to 10)
        for (i in 1..10) {
            val uuid = UUID.nameUUIDFromBytes("vehicle_$i".toByteArray()).toString()
            SupabaseIdMapper.registerMapping(i, uuid)
        }
        // Register deterministic UUIDs for standard static parts/defects (IDs 1 to 24)
        for (i in 1..24) {
            val uuid = UUID.nameUUIDFromBytes("part_$i".toByteArray()).toString()
            SupabaseIdMapper.registerMapping(i, uuid)
        }
    }

    // --- Seeding Initial Data ---
    suspend fun seedInitialDataIfEmpty() {
        try {
            // Seed local DB for offline fallback
            val count = db.vehicleDao().getAllVehicles().first().size
            if (count == 0) {
                seedLocalDb()
            }

            // Seed Supabase with default forum topics if it has none
            val remoteTopics = SupabaseClientProvider.client.postgrest["posts_comunidade"]
                .select()
                .decodeList<SupabasePostComunidade>()

            if (remoteTopics.isEmpty()) {
                seedSupabaseInitialData()
            }
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error seeding data on Supabase, falling back", e)
        }
    }

    private suspend fun seedLocalDb() {
        val defaultVehicles = getStaticVehicles()
        vehicleDao.insertVehicles(defaultVehicles)

        val defaultParts = getStaticParts()
        partDao.insertParts(defaultParts)

        val defaultTopics = listOf(
            ForumTopic(
                id = 1,
                title = "Barulho de ferro batendo na traseira do Gol G5 em buracos",
                author = "Jonathas Mecânico",
                body = "Estou com um Gol G5 1.0 na oficina. Faz um barulho metálico forte na traseira toda vez que passa em ondulações ou buracos pequenos. Já troquei os amortecedores e as buchas do eixo traseiro, mas o barulho continua. Alguém já pegou isso?",
                category = "Mecânica",
                isFollowed = true
            ),
            ForumTopic(
                id = 2,
                title = "Voyage 1.6 Flex com Luz EPC Acesa e sem aceleração",
                author = "Carlos_Eletro",
                body = "Colegas, esse veículo chegou de guincho. Não acelera nada, fica oscilando entre 1100 e 1300 RPM. Scanner acusa defeito no pedal do acelerador e TBI (Corpo de Borboleta). Já limpei o TBI e troquei o pedal para teste, mas não resolveu. Alguém tem o esquema elétrico do pedal até a ECU?",
                category = "Elétrica",
                isFollowed = false
            ),
            ForumTopic(
                id = 3,
                title = "Evitando manchas de emenda no retoque de Prata Metálico",
                author = "Mestre_Pintor",
                body = "Como vocês trabalham a transição de cor em para-lamas de carros prata para evitar que a emenda do verniz ou pigmento fique escura? Qual o melhor diluente de retoque (blender) para dar o acabamento perfeito?",
                category = "Funilaria/Pintura",
                isFollowed = false
            )
        )
        for (topic in defaultTopics) {
            forumDao.insertTopic(topic)
        }

        val defaultReplies = listOf(
            ForumReply(topicId = 1, author = "Marcos Suspensões", body = "Verifique o suporte superior do amortecedor traseiro (coxim). Costuma folgar a rosca superior ou o batente desgasta tanto que a haste bate no chassi. Outro ponto crítico são os cabos de freio de mão batendo no eixo traseiro!"),
            ForumReply(topicId = 1, author = "Edu_Car", body = "Exato, Marcos! Já peguei 3 casos aqui que o barulho parecia suspensão mas era apenas o protetor de escapamento traseiro solto batendo na carroceria ou os cabos de freio de mão sem as presilhas plásticas de fixação."),
            ForumReply(topicId = 2, author = "Geraldo Injeção", body = "Carlos, não mude mais peças. Meça a fiação entre o pedal e a central (ECU). No Voyage, Gol e Fox, o chicote que passa perto da bandeja da bateria costuma vibrar e quebrar o fio azul/vermelho por dentro da capa plástica. Faça o teste de continuidade puxando os fios delicadamente.")
        )
        for (reply in defaultReplies) {
            forumDao.insertReply(reply)
        }

        val defaultBadges = listOf(
            UserBadge(id = 1, name = "Primeiro Passo", description = "Acessou a plataforma e criou o perfil", iconName = "Handshake", isUnlocked = true, unlockedAt = System.currentTimeMillis()),
            UserBadge(id = 2, name = "Mecânico Colaborador", description = "Adicionou sua primeira contribuição ou dica técnica", iconName = "Build", isUnlocked = false),
            UserBadge(id = 3, name = "Doutor Automotivo", description = "Respondeu ou publicou uma dúvida no fórum", iconName = "Forum", isUnlocked = false),
            UserBadge(id = 4, name = "Mestre dos Manuais", description = "Visualizou detalhes de 3 manuais de peças e códigos", iconName = "Book", isUnlocked = false),
            UserBadge(id = 5, name = "Guardião Offline", description = "Salvou um manual ou tópico para acesso sem internet", iconName = "Download", isUnlocked = false)
        )
        badgeDao.insertBadges(defaultBadges)

        val defaultNotifications = listOf(
            InAppNotification(id = 1, title = "Bem-vindo ao AutoPedia!", body = "A maior enciclopédia técnica colaborativa para o setor automotivo brasileiro.", isRead = false),
            InAppNotification(id = 2, title = "Resposta no Tópico Seguido", body = "Marcos Suspensões respondeu ao tópico sobre 'Barulho de ferro batendo na traseira do Gol G5'.", isRead = false)
        )
        for (notif in defaultNotifications) {
            notificationDao.insertNotification(notif)
        }
    }

    private suspend fun seedSupabaseInitialData() {
        val userUuid = getOrCreateUserUuid("Jonathas Mecânico")
        
        // Seed default topics
        val defaultTopics = listOf(
            SupabasePostComunidade(
                id = UUID.nameUUIDFromBytes("topic_1".toByteArray()).toString(),
                usuarioId = userUuid,
                titulo = "Barulho de ferro batendo na traseira do Gol G5 em buracos",
                descricao = "Estou com um Gol G5 1.0 na oficina. Faz um barulho metálico forte na traseira toda vez que passa em ondulações ou buracos pequenos. Já troquei os amortecedores e as buchas do eixo traseiro, mas o barulho continua. Alguém já pegou isso?",
                resolvido = false
            ),
            SupabasePostComunidade(
                id = UUID.nameUUIDFromBytes("topic_2".toByteArray()).toString(),
                usuarioId = getOrCreateUserUuid("Carlos_Eletro"),
                titulo = "Voyage 1.6 Flex com Luz EPC Acesa e sem aceleração",
                descricao = "Colegas, esse veículo chegou de guincho. Não acelera nada, fica oscilando entre 1100 e 1300 RPM. Scanner acusa defeito no pedal do acelerador e TBI (Corpo de Borboleta). Já limpei o TBI e troquei o pedal para teste, mas não resolveu. Alguém tem o esquema elétrico do pedal até a ECU?",
                resolvido = false
            )
        )

        for (post in defaultTopics) {
            SupabaseClientProvider.client.postgrest["posts_comunidade"].insert(post)
        }

        // Seed default replies
        val defaultReplies = listOf(
            SupabaseComentario(
                id = UUID.randomUUID().toString(),
                postId = UUID.nameUUIDFromBytes("topic_1".toByteArray()).toString(),
                usuarioId = getOrCreateUserUuid("Marcos Suspensões"),
                conteudo = "Verifique o suporte superior do amortecedor traseiro (coxim). Costuma folgar a rosca superior ou o batente desgasta tanto que a haste bate no chassi. Outro ponto crítico são os cabos de freio de mão batendo no eixo traseiro!"
            ),
            SupabaseComentario(
                id = UUID.randomUUID().toString(),
                postId = UUID.nameUUIDFromBytes("topic_1".toByteArray()).toString(),
                usuarioId = getOrCreateUserUuid("Edu_Car"),
                conteudo = "Exato, Marcos! Já peguei 3 casos aqui que o barulho parecia suspensão mas era apenas o protetor de escapamento traseiro solto batendo na carroceria ou os cabos de freio de mão sem as presilhas plásticas de fixação."
            )
        )

        for (com in defaultReplies) {
            SupabaseClientProvider.client.postgrest["comentarios"].insert(com)
        }
    }

    // --- Vehicle Operations ---
    fun getAllVehicles(): Flow<List<Vehicle>> = flow {
        // Vehicles are catalog technical structures. We keep them locally or query them.
        // Return local list as it is fast, populated, and fully mapped to deterministic Supabase UUIDs!
        emit(getStaticVehicles())
    }

    fun searchVehicles(query: String?, brand: String?, year: Int?, type: String?): Flow<List<Vehicle>> = flow {
        var list = getStaticVehicles()
        if (!query.isNullOrBlank()) {
            val q = query.lowercase()
            list = list.filter { it.model.lowercase().contains(q) || it.brand.lowercase().contains(q) }
        }
        if (!brand.isNullOrBlank()) {
            list = list.filter { it.brand.equals(brand, ignoreCase = true) }
        }
        if (year != null) {
            list = list.filter { it.year == year }
        }
        if (!type.isNullOrBlank()) {
            list = list.filter { it.type.equals(type, ignoreCase = true) }
        }
        emit(list)
    }

    suspend fun insertVehicle(vehicle: Vehicle) {
        // Locally cached
        vehicleDao.insertVehicle(vehicle)
    }

    // --- Part and Defect Operations ---
    fun getPartsForVehicle(vehicleId: Int): Flow<List<PartAndDefect>> = flow {
        emit(getStaticParts().filter { it.vehicleId == vehicleId })
    }

    fun searchParts(query: String?, category: String?): Flow<List<PartAndDefect>> = flow {
        var list = getStaticParts()
        if (!query.isNullOrBlank()) {
            val q = query.lowercase()
            list = list.filter { it.name.lowercase().contains(q) || it.code.lowercase().contains(q) || it.chronicProblems.lowercase().contains(q) }
        }
        if (!category.isNullOrBlank()) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }
        emit(list)
    }

    suspend fun insertPart(part: PartAndDefect) {
        partDao.insertPart(part)
        badgeDao.unlockBadge("Mestre dos Manuais")
    }

    // --- Contribution Operations (mapped to posts_comunidade with custom metadata) ---
    fun getAllContributions(): Flow<List<UserContribution>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["posts_comunidade"]
                .select()
                .decodeList<SupabasePostComunidade>()
                .filter { it.versaoId != null }

            emit(list.map { post ->
                val vehicleIntId = post.versaoId?.let { SupabaseIdMapper.getIntId(it) } ?: 1
                val author = getUserName(post.usuarioId)
                UserContribution(
                    id = SupabaseIdMapper.getIntId(post.id),
                    vehicleId = vehicleIntId,
                    authorName = author,
                    title = post.titulo,
                    body = post.descricao ?: "",
                    timestamp = System.currentTimeMillis()
                )
            })
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting contributions", e)
            emit(contributionDao.getAllContributions().first())
        }
    }

    fun getContributionsForVehicle(vehicleId: Int): Flow<List<UserContribution>> = flow {
        try {
            val vehicleUuid = SupabaseIdMapper.getUuid(vehicleId)
            if (vehicleUuid == null) {
                emit(emptyList())
                return@flow
            }
            val list = SupabaseClientProvider.client.postgrest["posts_comunidade"]
                .select {
                    filter {
                        eq("versao_id", vehicleUuid)
                    }
                }
                .decodeList<SupabasePostComunidade>()

            emit(list.map { post ->
                val author = getUserName(post.usuarioId)
                UserContribution(
                    id = SupabaseIdMapper.getIntId(post.id),
                    vehicleId = vehicleId,
                    authorName = author,
                    title = post.titulo,
                    body = post.descricao ?: "",
                    timestamp = System.currentTimeMillis()
                )
            })
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting vehicle contributions", e)
            emit(contributionDao.getContributionsForVehicle(vehicleId).first())
        }
    }

    suspend fun insertContribution(contribution: UserContribution): Long {
        try {
            val userUuid = getOrCreateUserUuid(contribution.authorName)
            val vehicleUuid = contribution.vehicleId?.let { SupabaseIdMapper.getUuid(it) }

            val contributionUuid = UUID.randomUUID().toString()
            val post = SupabasePostComunidade(
                id = contributionUuid,
                usuarioId = userUuid,
                titulo = contribution.title,
                descricao = contribution.body,
                versaoId = vehicleUuid,
                resolvido = true
            )
            SupabaseClientProvider.client.postgrest["posts_comunidade"].insert(post)

            badgeDao.unlockBadge("Mecânico Colaborador")
            notificationDao.insertNotification(
                InAppNotification(
                    title = "Contribuição Publicada",
                    body = "Sua dica sobre '${contribution.title}' foi adicionada e está ajudando outros profissionais!"
                )
            )
            return SupabaseIdMapper.getIntId(contributionUuid).toLong()
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error inserting contribution", e)
            return contributionDao.insertContribution(contribution)
        }
    }

    // --- Forum Operations (mapped to posts_comunidade & comentarios) ---
    fun getAllTopics(): Flow<List<ForumTopic>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["posts_comunidade"]
                .select {
                    filter {
                        // Regular forum topics don't necessarily have a versao_id, or we fetch all
                    }
                }
                .decodeList<SupabasePostComunidade>()

            emit(list.map { post ->
                val author = getUserName(post.usuarioId)
                ForumTopic(
                    id = SupabaseIdMapper.getIntId(post.id),
                    title = post.titulo,
                    author = author,
                    body = post.descricao ?: "",
                    category = "Mecânica",
                    timestamp = System.currentTimeMillis(),
                    isFollowed = false
                )
            })
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting forum topics", e)
            emit(forumDao.getAllTopics().first())
        }
    }

    fun getTopicsByCategory(category: String): Flow<List<ForumTopic>> = flow {
        // Simple return of all topics for dynamic list UI
        emit(getAllTopics().first())
    }

    fun getTopicById(id: Int): Flow<ForumTopic?> = flow {
        try {
            val uuid = SupabaseIdMapper.getUuid(id)
            if (uuid == null) {
                emit(null)
                return@flow
            }
            val post = SupabaseClientProvider.client.postgrest["posts_comunidade"]
                .select {
                    filter {
                        eq("id", uuid)
                    }
                }
                .decodeSingleOrNull<SupabasePostComunidade>()

            if (post != null) {
                val author = getUserName(post.usuarioId)
                emit(ForumTopic(
                    id = id,
                    title = post.titulo,
                    author = author,
                    body = post.descricao ?: "",
                    category = "Mecânica",
                    timestamp = System.currentTimeMillis(),
                    isFollowed = false
                ))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting topic by ID", e)
            emit(forumDao.getTopicById(id).first())
        }
    }

    suspend fun insertTopic(topic: ForumTopic): Long {
        try {
            val userUuid = getOrCreateUserUuid(topic.author)
            val topicUuid = UUID.randomUUID().toString()

            val post = SupabasePostComunidade(
                id = topicUuid,
                usuarioId = userUuid,
                titulo = topic.title,
                descricao = topic.body,
                resolvido = false
            )
            SupabaseClientProvider.client.postgrest["posts_comunidade"].insert(post)
            badgeDao.unlockBadge("Doutor Automotivo")

            return SupabaseIdMapper.getIntId(topicUuid).toLong()
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error inserting forum topic", e)
            return forumDao.insertTopic(topic)
        }
    }

    suspend fun updateTopic(topic: ForumTopic) {
        // Skip or implement if needed
    }

    fun getRepliesForTopic(topicId: Int): Flow<List<ForumReply>> = flow {
        try {
            val topicUuid = SupabaseIdMapper.getUuid(topicId)
            if (topicUuid == null) {
                emit(emptyList())
                return@flow
            }
            val list = SupabaseClientProvider.client.postgrest["comentarios"]
                .select {
                    filter {
                        eq("post_id", topicUuid)
                    }
                }
                .decodeList<SupabaseComentario>()

            emit(list.map { com ->
                val author = getUserName(com.usuarioId)
                ForumReply(
                    id = com.id.hashCode() and 0x7FFFFFFF,
                    topicId = topicId,
                    author = author,
                    body = com.conteudo,
                    timestamp = System.currentTimeMillis()
                )
            })
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting topic replies", e)
            emit(forumDao.getRepliesForTopic(topicId).first())
        }
    }

    fun getRepliesCountByAuthor(author: String): Flow<Int> = flow {
        try {
            val userUuid = getOrCreateUserUuid(author)
            val list = SupabaseClientProvider.client.postgrest["comentarios"]
                .select {
                    filter {
                        eq("usuario_id", userUuid)
                    }
                }
                .decodeList<SupabaseComentario>()
            emit(list.size)
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting reply count", e)
            emit(forumDao.getRepliesCountByAuthor(author).first())
        }
    }

    suspend fun insertReply(reply: ForumReply): Long {
        try {
            val topicUuid = SupabaseIdMapper.getUuid(reply.topicId) ?: return 0L
            val userUuid = getOrCreateUserUuid(reply.author)
            val replyUuid = UUID.randomUUID().toString()

            val com = SupabaseComentario(
                id = replyUuid,
                postId = topicUuid,
                usuarioId = userUuid,
                conteudo = reply.body,
                marcadoComoSolucao = false
            )
            SupabaseClientProvider.client.postgrest["comentarios"].insert(com)
            badgeDao.unlockBadge("Doutor Automotivo")

            notificationDao.insertNotification(
                InAppNotification(
                    title = "Nova resposta no fórum",
                    body = "${reply.author} respondeu ao tópico que você acompanha."
                )
            )
            return (replyUuid.hashCode() and 0x7FFFFFFF).toLong()
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error inserting reply", e)
            return forumDao.insertReply(reply)
        }
    }

    // --- Saved Items / Offline Operations (handled locally via Room for immediate access) ---
    fun getAllSavedItems(): Flow<List<SavedItem>> = savedItemDao.getAllSavedItems()

    fun isItemSaved(type: String, refId: Int): Flow<Boolean> = savedItemDao.isItemSaved(type, refId)

    suspend fun saveItem(type: String, refId: Int, title: String, description: String) {
        savedItemDao.saveItem(SavedItem(type = type, referenceId = refId, title = title, description = description))
        badgeDao.unlockBadge("Guardião Offline")
        notificationDao.insertNotification(
            InAppNotification(
                title = "Conteúdo Salvo para Offline",
                body = "O manual '$title' foi salvo com sucesso e pode ser lido mesmo sem sinal de internet."
            )
        )
    }

    suspend fun removeSavedItem(type: String, refId: Int) {
        savedItemDao.deleteSavedItem(type, refId)
    }

    // --- Badges (local only) ---
    fun getAllBadges(): Flow<List<UserBadge>> = badgeDao.getAllBadges()

    suspend fun unlockBadge(badgeName: String) = badgeDao.unlockBadge(badgeName)

    // --- Notifications (local only) ---
    fun getAllNotifications(): Flow<List<InAppNotification>> = notificationDao.getAllNotifications()

    fun getUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()

    suspend fun insertNotification(title: String, body: String) {
        notificationDao.insertNotification(InAppNotification(title = title, body = body))
    }

    // --- Helper to get/create user on Supabase ---
    private suspend fun getOrCreateUserUuid(nome: String): String {
        try {
            val existing = SupabaseClientProvider.client.postgrest["usuarios"]
                .select {
                    filter {
                        eq("nome", nome)
                    }
                }
                .decodeList<SupabaseUsuario>().firstOrNull()

            if (existing != null) return existing.id

            val newId = UUID.randomUUID().toString()
            val newUser = SupabaseUsuario(
                id = newId,
                nome = nome,
                email = "${nome.lowercase().replace(" ", "")}@autopedia.com",
                tipoUsuario = "profissional"
            )
            SupabaseClientProvider.client.postgrest["usuarios"].insert(newUser)
            return newId
        } catch (e: Exception) {
            android.util.Log.e("MecanicoRepository", "Error getting/creating user", e)
            return UUID.nameUUIDFromBytes(nome.toByteArray()).toString()
        }
    }

    private suspend fun getUserName(userUuid: String): String {
        try {
            val user = SupabaseClientProvider.client.postgrest["usuarios"]
                .select {
                    filter {
                        eq("id", userUuid)
                    }
                }
                .decodeSingleOrNull<SupabaseUsuario>()
            return user?.nome ?: "Usuário"
        } catch (e: Exception) {
            return "Colaborador"
        }
    }

    suspend fun getOrCreateUserUuidPublic(nome: String): String {
        return getOrCreateUserUuid(nome)
    }

    suspend fun getStagingExtractions(): List<SupabaseExtracaoStaging> {
        return SupabaseClientProvider.client.postgrest["extracoes_staging"]
            .select {
                filter {
                    eq("status_revisao", "pendente")
                }
            }
            .decodeList<SupabaseExtracaoStaging>()
    }

    suspend fun getEnriquecimentoWebLogs(): List<SupabaseEnriquecimentoWebLog> {
        return SupabaseClientProvider.client.postgrest["enriquecimento_web_log"]
            .select()
            .decodeList<SupabaseEnriquecimentoWebLog>()
    }

    // --- Static Catalog Fallback ---
    private fun getStaticVehicles(): List<Vehicle> {
        return listOf(
            Vehicle(id = 1, type = "Carro", brand = "Chevrolet", model = "Onix", year = 2021, manufacturer = "GM"),
            Vehicle(id = 2, type = "Carro", brand = "Fiat", model = "Strada", year = 2022, manufacturer = "Fiat"),
            Vehicle(id = 3, type = "Carro", brand = "Volkswagen", model = "Gol G5", year = 2011, manufacturer = "VW"),
            Vehicle(id = 4, type = "Carro", brand = "Hyundai", model = "HB20", year = 2020, manufacturer = "Hyundai"),
            Vehicle(id = 5, type = "Carro", brand = "Toyota", model = "Corolla", year = 2019, manufacturer = "Toyota"),
            Vehicle(id = 6, type = "Moto", brand = "Honda", model = "CG 160 Titan", year = 2022, manufacturer = "Honda"),
            Vehicle(id = 7, type = "Moto", brand = "Yamaha", model = "Factor 150", year = 2021, manufacturer = "Yamaha"),
            Vehicle(id = 8, type = "Moto", brand = "Honda", model = "Biz 125", year = 2020, manufacturer = "Honda"),
            Vehicle(id = 9, type = "Caminhão", brand = "Volkswagen", model = "Constellation 24.280", year = 2018, manufacturer = "VW"),
            Vehicle(id = 10, type = "Caminhão", brand = "Mercedes-Benz", model = "Accelo 1016", year = 2020, manufacturer = "Mercedes-Benz")
        )
    }

    private fun getStaticParts(): List<PartAndDefect> {
        return listOf(
            PartAndDefect(
                id = 1,
                vehicleId = 1,
                name = "Correia Dentada Banhada a Óleo",
                code = "GM-12730822",
                serialNumber = "SN-ONX-7821",
                category = "Mecânica",
                chronicProblems = "Desgaste prematuro e descamação da correia se não for utilizado o óleo sintético exato (5W30 Dexos1 Gen2) ou se houver atraso na troca. Os resíduos de borracha descascada caem no cárter e entopem o pescador da bomba de óleo, levando à perda de pressão de óleo e fundição do motor de 3 cilindros.",
                diagramUrl = "Esquema de Sincronismo: Polia do comando de admissão, Polia de escape, Tensionador automático, Polia do virabrequim. Posição de sincronismo em PMS.",
                imageUrl = "Correia desgastada apresentando trincas e fiapos de borracha soltos. Pescador de óleo completamente obstruído por fragmentos pretos de borracha."
            ),
            PartAndDefect(
                id = 2,
                vehicleId = 1,
                name = "Vela de Ignição Iridium",
                code = "GM-12683541",
                serialNumber = "SN-ONX-5531",
                category = "Elétrica",
                chronicProblems = "Infiltração de água no alojamento das velas após lavagem do motor ou chuva forte. Causa oxidação do terminal da bobina e falha de combustão (misfire), acendendo a luz da injeção no painel e fazendo o motor tremer.",
                diagramUrl = "Conexão Elétrica: Bobina individual de 4 pinos ligada diretamente à ECU do motor nos pinos 23, 24 e 25.",
                imageUrl = "Vela de ignição com marcas de ferrugem na cerâmica branca e água acumulada no fundo do poço da vela."
            ),
            PartAndDefect(
                id = 3,
                vehicleId = 1,
                name = "Bobina de Ignição Integrada",
                code = "GM-12613000",
                serialNumber = "SN-ONX-9002",
                category = "Elétrica",
                chronicProblems = "Trincas microscópicas no corpo de baquelite que isola as saídas de alta tensão para as velas. Isso gera fuga de corrente para o bloco do motor, gerando falhas intermitentes sob carga (retomadas de velocidade).",
                diagramUrl = "Conector Elétrica: Bobina de faísca perdida com conector primário de 4 vias diretamente da ECU.",
                imageUrl = "Bobina de ignição com marcas esbranquiçadas de fuga de corrente ao longo do corpo isolante."
            ),
            PartAndDefect(
                id = 4,
                vehicleId = 2,
                name = "Coxim Inferior do Motor",
                code = "FT-51829032",
                serialNumber = "SN-STR-3011",
                category = "Mecânica",
                chronicProblems = "Ruptura prematura da borracha vulcânica do coxim limitador de torque traseiro (conhecido como raquete). Causa trancos fortes nas arrancadas, vibração excessiva no painel de instrumentos e ruído metálico seco ao engatar marcha ré.",
                diagramUrl = "Esquema de Fixação: Fixação central no agregado de suspensão dianteira com parafuso M10 torque de 80 Nm.",
                imageUrl = "Coxim com a borracha central totalmente rasgada e descolada do suporte de alumínio."
            ),
            PartAndDefect(
                id = 5,
                vehicleId = 2,
                name = "Amortecedor Dianteiro",
                code = "FT-52039011",
                serialNumber = "SN-STR-4921",
                category = "Mecânica",
                chronicProblems = "Desgaste precoce do batente interno e vazamento de fluido hidráulico pelo retentor da haste ao trafegar com frequência em estradas de terra ou paralelepípedo com carga.",
                diagramUrl = "Componentes: Amortecedor telescópico, mola helicoidal, coxim superior com rolamento, batente elástico e coifa protetora.",
                imageUrl = "Haste do amortecedor melada de óleo hidráulico e batente interno esfarelado."
            ),
            PartAndDefect(
                id = 6,
                vehicleId = 2,
                name = "Feixe de Molas Traseiro",
                code = "FT-51928312",
                serialNumber = "SN-STR-8813",
                category = "Mecânica",
                chronicProblems = "Ruído alto de rangido constante ('nhec-nhec') na suspensão traseira. Ocorre devido ao desgaste precoce das pastilhas silenciadoras de náilon situadas nas pontas das lâminas do feixe de molas.",
                diagramUrl = "Suspensão Traseira: Lâmina mestra, lâminas auxiliares, abraçadeiras de guia, jumelos e bucha traseira silenciosa.",
                imageUrl = "Lâminas de mola raspando diretamente metal com metal devido à perda total das pastilhas plásticas."
            ),
            PartAndDefect(
                id = 7,
                vehicleId = 3,
                name = "Corpo de Borboleta (TBI) Magneti Marelli",
                code = "VW-030133062D",
                serialNumber = "SN-GOL-9921",
                category = "Mecânica",
                chronicProblems = "Perda de aceleração repentina (limp mode) e luz do EPC acesa no painel. Ocorre devido ao desgaste mecânico das engrenagens plásticas internas do motor de passo do TBI. O motor fica sem força e preso em marcha lenta acelerada.",
                diagramUrl = "Conector TBI: Pino 1 (Motor +), Pino 2 (Motor -), Pino 3 (Potenciômetro 2), Pino 4 (Alimentação 5V), Pino 5 (Potenciômetro 1), Pino 6 (Negativo).",
                imageUrl = "Vista interna do TBI mostrando engrenagens de plástico com dentes quebrados ou espanados."
            ),
            PartAndDefect(
                id = 8,
                vehicleId = 3,
                name = "Sensor de Nível de Combustível",
                code = "VW-5U0919051",
                serialNumber = "SN-GOL-4512",
                category = "Elétrica",
                chronicProblems = "Oxidação química precoce da pista de resistência cerâmica devido ao uso contínuo de etanol de baixa qualidade. Causa oscilação louca ou indicação de tanque totalmente vazio mesmo estando cheio.",
                diagramUrl = "Montagem: Fixado na lateral do copo do módulo da bomba de combustível dentro do tanque sob o banco traseiro.",
                imageUrl = "Placa de cerâmica do sensor com as trilhas pretas de leitura totalmente gastas e pretas."
            ),
            PartAndDefect(
                id = 9,
                vehicleId = 4,
                name = "Catalisador de Escapamento",
                code = "HY-28510030",
                serialNumber = "SN-HB2-1209",
                category = "Mecânica",
                chronicProblems = "Trinca interna na cerâmica do elemento catalítico por choque térmico ou excesso de combustível não queimado. Causa barulho de chocalho sob o motor, acende a luz de injeção acusando erro P0420 e perde potência.",
                diagramUrl = "Posicionamento: Integrado ao coletor de escape logo na saída do bloco do motor, antes da sonda lambda.",
                imageUrl = "Vista interna do coletor with a colmeia cerâmica interna do catalisador totalmente derretida ou esfarelada."
            ),
            PartAndDefect(
                id = 10,
                vehicleId = 4,
                name = "Caixa de Direção Mecânica",
                code = "HY-577001S0",
                serialNumber = "SN-HB2-7711",
                category = "Mecânica",
                chronicProblems = "Folga excessiva e ruído metálico forte ('clec-clec') ao esterçar o volante em baixa velocidade ou manobrar em pisos irregulares. Causado pelo desgaste prematuro da bucha de teflon interna do lado direito.",
                diagramUrl = "Conexão: Coluna de direção -> Pinhão -> Cremalheira -> Buchas guia -> Terminais de direção.",
                imageUrl = "Bucha interna de teflon deformada e esmagada com folga acentuada no diâmetro do eixo da cremalheira."
            )
        )
    }
}

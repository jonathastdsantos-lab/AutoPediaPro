package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class MecanicoRepository(private val db: AppDatabase) {

    private val vehicleDao = db.vehicleDao()
    private val partDao = db.partDao()
    private val contributionDao = db.contributionDao()
    private val forumDao = db.forumDao()
    private val savedItemDao = db.savedItemDao()
    private val badgeDao = db.badgeDao()
    private val notificationDao = db.notificationDao()

    // --- Seeding Initial Data ---
    suspend fun seedInitialDataIfEmpty() {
        val count = db.vehicleDao().getAllVehicles().first().size
        if (count > 0) return

        // 1. Seed Vehicles
        val defaultVehicles = listOf(
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
        vehicleDao.insertVehicles(defaultVehicles)

        // 2. Seed Parts and Chronic Defects
        val defaultParts = listOf(
            // Chevrolet Onix (Id 1)
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
            // Fiat Strada (Id 2)
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
            // VW Gol G5 (Id 3)
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
            // Hyundai HB20 (Id 4)
            PartAndDefect(
                id = 9,
                vehicleId = 4,
                name = "Catalisador de Escapamento",
                code = "HY-28510030",
                serialNumber = "SN-HB2-1209",
                category = "Mecânica",
                chronicProblems = "Trinca interna na cerâmica do elemento catalítico por choque térmico ou excesso de combustível não queimado. Causa barulho de chocalho sob o motor, acende a luz de injeção acusando erro P0420 e perde potência.",
                diagramUrl = "Posicionamento: Integrado ao coletor de escape logo na saída do bloco do motor, antes da sonda lambda.",
                imageUrl = "Vista interna do coletor com a colmeia cerâmica interna do catalisador totalmente derretida ou esfarelada."
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
            ),
            // Toyota Corolla (Id 5)
            PartAndDefect(
                id = 11,
                vehicleId = 5,
                name = "Amortecedor Dianteiro",
                code = "TY-48510802",
                serialNumber = "SN-COR-0012",
                category = "Mecânica",
                chronicProblems = "Vazamento prematuro de fluido hidráulico pelo retentor da haste superior e forte ruído de batida seca na suspensão dianteira ao passar por quebra-molas ou emendas de asfalto.",
                diagramUrl = "Conjunto Mcpherson: Amortecedor hidráulico pressurizado a gás, mola helicoidal, coxim com batente elástico superior.",
                imageUrl = "Retentor superior trincado com marcas visíveis de vazamento abundante de óleo hidráulico sobre a carcaça."
            ),
            PartAndDefect(
                id = 12,
                vehicleId = 5,
                name = "Junta Homocinética Lado Roda",
                code = "TY-43410123",
                serialNumber = "SN-COR-5050",
                category = "Mecânica",
                chronicProblems = "Estalos metálicos constantes ao esterçar a direção totalmente para um dos lados e tracionar o veículo. Geralmente causado pela ruptura da coifa protetora de borracha, permitindo entrada de poeira e perda de graxa.",
                diagramUrl = "Transmissão: Semieixo -> Junta Homocinética -> Coifa com abraçadeiras metálicas -> Cubo de roda.",
                imageUrl = "Coifa de borracha rasgada ao meio com graxa grafita expelida e esferas metálicas com desgaste visível."
            ),
            // Honda CG 160 (Id 6)
            PartAndDefect(
                id = 13,
                vehicleId = 6,
                name = "Tensionador da Corrente de Comando",
                code = "HD-14520-KRE-G01",
                serialNumber = "SN-CG-3021",
                category = "Mecânica",
                chronicProblems = "Perda de pressão interna da mola espiral do tensionador automático. Causa folga na corrente de comando interna do motor, gerando um ruído metálico forte batendo ('tec-tec-tec') constante que aumenta com a rotação.",
                diagramUrl = "Vista explodida: Cilindro do motor -> Guia da corrente -> Corrente de comando -> Tensionador traseiro fixado por 2 parafusos M6.",
                imageUrl = "Ponta do tensionador retraída sem força de empuxo na corrente, comparado ao novo com mola firme."
            ),
            PartAndDefect(
                id = 14,
                vehicleId = 6,
                name = "Bomba de Combustível Interna",
                code = "HD-16700-KVS-J01",
                serialNumber = "SN-CG-1188",
                category = "Mecânica",
                chronicProblems = "Perda acentuada de pressão hidráulica (abaixo de 3.0 bar) quando a motocicleta é utilizada sob temperaturas ambientes elevadas. Faz o motor falhar em altas rotações ou apagar por completo, só religando após esfriar.",
                diagramUrl = "Alimentação: Bomba elétrica interna ao tanque, pré-filtro inferior, regulador de pressão de 3.0 bar calibrado.",
                imageUrl = "Pré-filtro de combustível completamente preto por acúmulo de sujeira e resíduos de combustível."
            ),
            // Yamaha Factor (Id 7)
            PartAndDefect(
                id = 15,
                vehicleId = 7,
                name = "Placa de Partida Unidirecional",
                code = "YM-1S9E5560",
                serialNumber = "SN-FAC-2019",
                category = "Mecânica",
                chronicProblems = "Desgaste mecânico acentuado nos roletes de engate interno ou enfraquecimento das molas limitadoras. O motor de partida gira em falso com ruído agudo metálico, sem engrenar e virar o virabrequim da motocicleta.",
                diagramUrl = "Sincronismo de Partida: Engrenagem do motor elétrico -> Engrenagem intermediária de redução -> Placa de partida fixada no magneto.",
                imageUrl = "Corpo da placa de partida trincado ao redor dos alojamentos dos roletes de aço temperado."
            ),
            PartAndDefect(
                id = 16,
                vehicleId = 7,
                name = "Sensor de Posição do Virabrequim (CKP)",
                code = "YM-5HH81410",
                serialNumber = "SN-FAC-7732",
                category = "Elétrica",
                chronicProblems = "Bobina pulsadora do sensor CKP apresenta circuito aberto interno intermitente quando atinge temperaturas próximas a 90°C. Interrompe a geração de faísca na vela e o motor apaga do nada, voltando a funcionar quando o motor esfria.",
                diagramUrl = "Conexão Elétrica: Sensor fixado na tampa do estator, fios azul/branco e verde/branco ligados ao CDI/ECU.",
                imageUrl = "Sensor CKP com sinais de derretimento do plástico protetor devido à alta temperatura do óleo do motor."
            ),
            // Honda Biz 125 (Id 8)
            PartAndDefect(
                id = 17,
                vehicleId = 8,
                name = "Kit de Embreagem Centrífuga",
                code = "HD-22300-KPN-305",
                serialNumber = "SN-BIZ-4411",
                category = "Mecânica",
                chronicProblems = "Desgaste prematuro das sapatas de fricção centrífuga e fadiga das molas de retorno rápido. Faz a moto patinar excessivamente nas saídas de semáforo, além de perder força de subida nas ladeiras íngremes.",
                diagramUrl = "Esquema da Embreagem Dupla: Embreagem centrífuga primária acoplada ao virabrequim, embreagem multi-disco secundária no eixo piloto.",
                imageUrl = "Sapatas centrífugas totalmente gastas, sem material de fricção (lona), arranhando o tambor de aço."
            ),
            PartAndDefect(
                id = 18,
                vehicleId = 8,
                name = "Interruptor de Neutro",
                code = "HD-35759-KTL-741",
                serialNumber = "SN-BIZ-9931",
                category = "Elétrica",
                chronicProblems = "Infiltração de óleo de motor pelo retentor de borracha interno do sensor de neutro. Isso isola o contato elétrico e impede o acendimento da lâmpada indicadora 'N' no painel, impossibilitando a partida elétrica.",
                diagramUrl = "Fiação: Sensor de neutro fixado próximo ao pinhão, cabo de contato ligado diretamente ao relé de partida e lâmpada.",
                imageUrl = "Sensor removido com contatos de cobre recobertos por borra preta de óleo carbonizado isolante."
            ),
            // Volkswagen Constellation (Id 9)
            PartAndDefect(
                id = 19,
                vehicleId = 9,
                name = "Válvula de Controle do Turbo (N75)",
                code = "VW-2T2906283",
                serialNumber = "SN-CON-6021",
                category = "Mecânica",
                chronicProblems = "Entupimento por fuligem fina ou queima eletromecânica do solenoide de controle de vácuo. Causa perda brusca de torque do caminhão em subidas carregadas, ativando luz de falha grave de motor no painel.",
                diagramUrl = "Vácuo do Turbo: Válvula reguladora, mangueiras de vácuo ligadas ao atuador Wastegate do compressor turbo compressor.",
                imageUrl = "Válvula N75 aberta com fuligem preta obstruindo as passagens finas de ar e membrana de borracha interna rasgada."
            ),
            PartAndDefect(
                id = 20,
                vehicleId = 9,
                name = "Compressor de Ar do Freio",
                code = "VW-2R0137021",
                serialNumber = "SN-CON-9021",
                category = "Mecânica",
                chronicProblems = "Passagem excessiva de óleo lubrificante do cárter para a linha de descarga de pressão pneumática por desgaste prematuro dos anéis de pistão. Contamina e estraga as válvulas APU, secadora de ar e cilindros de freio.",
                diagramUrl = "Linha de Ar: Compressor pneumático monocilíndrico, serpentina de resfriamento, válvula secadora regenerativa APU.",
                imageUrl = "Mangueira de saída do compressor com grossa camada de carvão misturada com óleo lubrificante queimado."
            ),
            // Mercedes Accelo (Id 10)
            PartAndDefect(
                id = 21,
                vehicleId = 10,
                name = "Módulo Sensor de NoX (Sistema SCR)",
                code = "MB-A0009053403",
                serialNumber = "SN-ACC-8812",
                category = "Elétrica",
                chronicProblems = "Queima do aquecedor interno do sensor de NoX devido a choque térmico com água no escapamento, ou cristalização do reagente Arla 32 no bico dosador. Limita o torque do caminhão em até 40% para cumprir leis ambientais.",
                diagramUrl = "Pinagem CAN do Sensor de NoX: Pino 1 (VCC 24V), Pino 2 (GND), Pino 3 (CAN High), Pino 4 (CAN Low) conectados ao barramento do veículo.",
                imageUrl = "Sonda de NoX esbranquiçada e coberta por crostas de amônia sólida cristalizada impedindo a leitura de gases de escape."
            ),
            PartAndDefect(
                id = 22,
                vehicleId = 10,
                name = "Bomba Dosadora de Arla 32",
                code = "MB-A0001407578",
                serialNumber = "SN-ACC-1102",
                category = "Mecânica",
                chronicProblems = "Entupimento ou travamento das engrenagens da bomba de Arla por uso de agente redutor adulterado ou cristalização pela falta de purga automática após desligar o motor do caminhão.",
                diagramUrl = "Fluxo: Tanque de Arla 32 -> Bomba dosadora -> Filtro principal -> Bico injetor no catalisador de escape.",
                imageUrl = "Cabeçote da bomba dosadora aberto revelando grandes cristais brancos de ureia travando os eixos."
            ),
            // Paint and Bodywork Special Components (Id 2 e Id 1)
            PartAndDefect(
                id = 23,
                vehicleId = 2,
                name = "Verniz Poliuretano Alto Sólido (Pintura)",
                code = "PX-8000-AS",
                serialNumber = "SN-PTU-8021",
                category = "Pintura",
                chronicProblems = "Descascamento prematuro ou desbotamento do verniz em partes planas expostas à radiação solar (capô e teto). Causado pela aplicação com dosagem incorreta do catalisador endurecedor ou falta de espessura de filme seco recomendada.",
                diagramUrl = "Camadas: Chapa de aço -> Primer fosfatante -> Primer Poliuretano -> Base poliéster cor -> Verniz Poliuretano (2 demaos).",
                imageUrl = "Superfície da pintura com o verniz esbranquiçado se soltando em placas, expondo a tinta fosca por baixo."
            ),
            PartAndDefect(
                id = 24,
                vehicleId = 1,
                name = "Primer Fosfatante (Wash Primer)",
                code = "PX-1200-WP",
                serialNumber = "SN-PTU-1200",
                category = "Pintura",
                chronicProblems = "Bolhas de ar e perda total de aderência da tinta nas superfícies de aço galvanizado ou alumínio de para-lamas novos. Ocorre quando não se aplica o Wash Primer antes do Primer PU tradicional, levando a focos de ferrugem por baixo da pintura.",
                diagramUrl = "Esquema: Aplicação de 1 demão fina (espessura de 5 a 10 mícrons) sobre chapa nua e lixada, aguardar 20 min antes do Primer PU.",
                imageUrl = "Pintura se descascando facilmente ao jatear água, revelando o aço brilhante sem ancoragem por baixo."
            )
        )
        partDao.insertParts(defaultParts)

        // 3. Seed Forum Topics
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

        // 4. Seed Forum Replies
        val defaultReplies = listOf(
            ForumReply(topicId = 1, author = "Marcos Suspensões", body = "Verifique o suporte superior do amortecedor traseiro (coxim). Costuma folgar a rosca superior ou o batente desgasta tanto que a haste bate no chassi. Outro ponto crítico são os cabos de freio de mão batendo no eixo traseiro!"),
            ForumReply(topicId = 1, author = "Edu_Car", body = "Exato, Marcos! Já peguei 3 casos aqui que o barulho parecia suspensão mas era apenas o protetor de escapamento traseiro solto batendo na carroceria ou os cabos de freio de mão sem as presilhas plásticas de fixação."),
            ForumReply(topicId = 2, author = "Geraldo Injeção", body = "Carlos, não mude mais peças. Meça a fiação entre o pedal e a central (ECU). No Voyage, Gol e Fox, o chicote que passa perto da bandeja da bateria costuma vibrar e quebrar o fio azul/vermelho por dentro da capa plástica. Faça o teste de continuidade puxando os fios delicadamente.")
        )
        for (reply in defaultReplies) {
            forumDao.insertReply(reply)
        }

        // 5. Seed Badges
        val defaultBadges = listOf(
            UserBadge(id = 1, name = "Primeiro Passo", description = "Acessou a plataforma e criou o perfil", iconName = "Handshake", isUnlocked = true, unlockedAt = System.currentTimeMillis()),
            UserBadge(id = 2, name = "Mecânico Colaborador", description = "Adicionou sua primeira contribuição ou dica técnica", iconName = "Build", isUnlocked = false),
            UserBadge(id = 3, name = "Doutor Automotivo", description = "Respondeu ou publicou uma dúvida no fórum", iconName = "Forum", isUnlocked = false),
            UserBadge(id = 4, name = "Mestre dos Manuais", description = "Visualizou detalhes de 3 manuais de peças e códigos", iconName = "Book", isUnlocked = false),
            UserBadge(id = 5, name = "Guardião Offline", description = "Salvou um manual ou tópico para acesso sem internet", iconName = "Download", isUnlocked = false)
        )
        badgeDao.insertBadges(defaultBadges)

        // 6. Seed In-App Notifications
        val defaultNotifications = listOf(
            InAppNotification(id = 1, title = "Bem-vindo ao AutoPedia!", body = "A maior enciclopédia técnica colaborativa para o setor automotivo brasileiro.", isRead = false),
            InAppNotification(id = 2, title = "Resposta no Tópico Seguido", body = "Marcos Suspensões respondeu ao tópico sobre 'Barulho de ferro batendo na traseira do Gol G5'.", isRead = false)
        )
        for (notif in defaultNotifications) {
            notificationDao.insertNotification(notif)
        }
    }

    // --- Vehicle Operations ---
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun searchVehicles(query: String?, brand: String?, year: Int?, type: String?): Flow<List<Vehicle>> =
        vehicleDao.searchVehicles(query, brand, year, type)

    suspend fun insertVehicle(vehicle: Vehicle) = vehicleDao.insertVehicle(vehicle)

    // --- Part and Defect Operations ---
    fun getPartsForVehicle(vehicleId: Int): Flow<List<PartAndDefect>> = partDao.getPartsForVehicle(vehicleId)

    fun searchParts(query: String?, category: String?): Flow<List<PartAndDefect>> = partDao.searchParts(query, category)

    suspend fun insertPart(part: PartAndDefect) {
        partDao.insertPart(part)
        // Trigger potential badge unlock
        badgeDao.unlockBadge("Mestre dos Manuais")
    }

    // --- Contribution Operations ---
    fun getAllContributions(): Flow<List<UserContribution>> = contributionDao.getAllContributions()

    fun getContributionsForVehicle(vehicleId: Int): Flow<List<UserContribution>> = contributionDao.getContributionsForVehicle(vehicleId)

    suspend fun insertContribution(contribution: UserContribution): Long {
        val id = contributionDao.insertContribution(contribution)
        // Unlock badge for contribution
        badgeDao.unlockBadge("Mecânico Colaborador")
        // Post an in-app notification
        notificationDao.insertNotification(
            InAppNotification(
                title = "Contribuição Publicada",
                body = "Sua dica sobre '${contribution.title}' foi adicionada e está ajudando outros profissionais!"
            )
        )
        return id
    }

    // --- Forum Operations ---
    fun getAllTopics(): Flow<List<ForumTopic>> = forumDao.getAllTopics()

    fun getTopicsByCategory(category: String): Flow<List<ForumTopic>> = forumDao.getTopicsByCategory(category)

    fun getTopicById(id: Int): Flow<ForumTopic?> = forumDao.getTopicById(id)

    suspend fun insertTopic(topic: ForumTopic): Long {
        val id = forumDao.insertTopic(topic)
        badgeDao.unlockBadge("Doutor Automotivo")
        return id
    }

    suspend fun updateTopic(topic: ForumTopic) = forumDao.updateTopic(topic)

    fun getRepliesForTopic(topicId: Int): Flow<List<ForumReply>> = forumDao.getRepliesForTopic(topicId)

    suspend fun insertReply(reply: ForumReply): Long {
        val id = forumDao.insertReply(reply)
        badgeDao.unlockBadge("Doutor Automotivo")

        // Trigger simulated notifications for other users
        // If author is not "Você", simulate a notification
        notificationDao.insertNotification(
            InAppNotification(
                title = "Nova resposta no fórum",
                body = "${reply.author} respondeu ao tópico que você acompanha."
            )
        )
        return id
    }

    // --- Saved Items / Offline Operations ---
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

    // --- Badges ---
    fun getAllBadges(): Flow<List<UserBadge>> = badgeDao.getAllBadges()

    suspend fun unlockBadge(badgeName: String) = badgeDao.unlockBadge(badgeName)

    // --- Notifications ---
    fun getAllNotifications(): Flow<List<InAppNotification>> = notificationDao.getAllNotifications()

    fun getUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()

    suspend fun insertNotification(title: String, body: String) {
        notificationDao.insertNotification(InAppNotification(title = title, body = body))
    }
}

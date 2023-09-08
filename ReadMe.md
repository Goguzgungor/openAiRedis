## Spring-Redis ile OpenAI Entegrasyonu
Hackathon ya da diğer yazılım geliştirme süreçlerinde bir 3rd party sistemi yazıp daha sonra bunu standalone olarak her projede kullanabilmek biz yazılım geliştiriciler için önemli. Daha önce bir projemde standalone çalışacak şekilde bir Redis-OpenAi entegrasyonu yazmıştım. Tüm yazılanları adım adım yaparsanız yaklaşık 15 dakikada uygulamanıza Yapay Zeka entegrasyonu ekleyebilirsiniz diye öngörüyorum. Hadi başlayalım!

Hatırlatma: Kodlar biraz spagetti olabilir çünkü bu yazıyı yazarken kodlarımda biraz değişiklik yaptım. Ama sizin için önemli olan adımları anlamak. Kodları daha sonra düzenleyeceğim.
## Redis Nedir?
Redis, açık kaynaklı, ağ üzerinde çalışan, anahtar/değer veri yapısını kullanan, NoSQL veritabanı yönetim sistemidir. Redis, verileri bellekte tutar. Bu nedenle veri okuma ve yazma işlemleri çok hızlıdır. Biz rediste chat mesajlarımızı tutacağız. Redis ile ilgili daha fazla bilgi için [Redis](https://redis.io/) adresini ziyaret edebilirsiniz.
![Redis](https://ps.w.org/redis-cache/assets/banner-1544x500.png?rev=2315420)
## Redis Kurulumu
Redis kurulumu için [Redis](https://redis.io/docs/getting-started/) adresinden işletim sisteminize uygun olanı indirip kurabilirsiniz. Ben Windows için olanın nasıl kurulacağını bulabilirsiniz. Macos için ise aşağıdaki komutu çalıştırabilirsiniz.
```
brew install redis
```
Redisin kurulumunu tamamladıktan sonra redis-server komutu ile redis sunucusunu başlatabilirsiniz. Redis sunucusunu başlattıktan sonra redis-cli komutu ile redis sunucusuna bağlanabilirsiniz. Redis sunucusuna bağlandıktan sonra aşağıdaki komutları çalıştırarak test edebilirsiniz.
```
redis-server
```
## OpenAI Nedir?
OpenAI, yapay zeka araştırmaları yapan bir şirkettir. OpenAI, yapay zeka araştırmalarını insanlığın yararına olacak şekilde yapmayı amaçlamaktadır. OpenAI ile ilgili daha fazla bilgi için [OpenAI](https://openai.com/) adresini ziyaret edebilirsiniz. Biz genel olarak OpenAI'ın bize vermiş olduğu api servislerini kullancağız. Bunları kullanmak için öncelik bir hesap oluşturmanız gerekiyor. Hesabınızı oluşturduktan sonra [OpenAI](https://platform.openai.com/) adresinden api keyinizi alabilirsiniz.
![OpenAI](https://venturebeat.com/wp-content/uploads/2019/03/openai-1.png?fit=2400%2C1000&strip=all)
## OpenAI Apileri nasıl çalışır?
OpenAI'ın bize sunduğu api servisleri ile yapay zeka modellerini kullanabiliriz. Bu modelleri kullanmak için öncelikle bir model oluşturmanız gerekiyor. Model oluşturduktan sonra bu modele promptlar ekleyebilirsiniz. Promptlar ekledikten sonra modeli eğitebilirsiniz. Modeli eğittikten sonra modeli kullanabilirsiniz. Modeli kullanmak için promptlarınızı göndermeniz yeterli olacaktır. Modeliniz size bir cevap döndürecektir. Bu cevabı kullanarak uygulamanızda yapmak istediğiniz işlemleri yapabilirsiniz. Örneğin bir chat uygulaması yazıyorsanız kullanıcıdan gelen mesajı prompt olarak gönderip modelden dönen cevabı kullanıcıya gönderebilirsiniz. Bu şekilde kullanıcı ile yapay zeka arasında bir sohbet başlatabilirsiniz. OpenAI ile ilgili daha fazla bilgi için [OpenAI](https://beta.openai.com/docs/introduction) adresini ziyaret edebilirsiniz.
Aşağıdaki gibi göndereceğimiz bir post isteği ile model oluşturabiliriz.
```
curl https://api.openai.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [
      {
        "role": "system",
        "content": "You are a helpful assistant."
      },
      {
        "role": "user",
        "content": "Hello!"
      }
    ]
  }'
```
Bu isteğin cevabı aşağıdaki gibi olacaktır.
```
{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "gpt-3.5-turbo-0613",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "\n\nHello there, how may I assist you today?",
    },
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 9,
    "completion_tokens": 12,
    "total_tokens": 21
  }
}
```
## Sistem Nasıl Çalışacak?
Yukarıdaki örnekte farkedeceğiniz ilk şey modelin bizimle alakalı olan eğitiminin içine gönderdiğimiz mesaj kadar olduğu olacaktır. Bu yüzden düzgün bir chat akışı için Redis'i bir geçici bir depo olarak kullanacağız. Geçici diyorum çünkü redis sürekli veri depolamak için uygun bir teknoloji değil. Benim de burda yapmak istediğim kullanıcı için o anlık bir yapay zeka desteği sağlamak. Yani bu projedeki asistan ChatGPT'nin yaptığı gibi bütün konuşmaları hatırlamayacak. Eğer mesajları tutmak için Redis kullanıyorsanız chat bittikten sonra o kullanıcının redis üzerindeki verilerini silmeniz en doğrusu olacaktır. Bunun yanında redis okuma-yazma konusunda çok hızlı olması dolayısıyla bizim için işleri hızlandıracaktır.
![Sistem](https://api.profil-software.com/media/images/7_gAxtqg2.png)

## Gerekli Dependenyleri Ekleyelim
Aşağıdaki Dependencyleri pom.xml dosyamıza ekleyelim. Versionları değiştirmeden ekledeğinizden ya da birbirleriyle uyumlu olduklarından emin olun.
```
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
			<version>3.1.1</version>
		</dependency>
		
		<dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
			<version>4.4.3</version>
		</dependency>
```
## Redis Configuration
Redis ile bağlantı kurmak için RedisConfiguration sınıfını oluşturalım. Bu sınıfı oluşturduktan sonra Redis ile bağlantı kurmak için gerekli olan bilgileri application.properties dosyasına ekleyelim. Ben localde çalışacağım için saklamaya gerek duymadım.
```
@Configuration
public class RedisConfig{

    @Bean
    public RedisTemplate<String, AssistantEntity> redisTemplate() {
        RedisTemplate<String, AssistantEntity> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory
                = new JedisConnectionFactory();
        jedisConFactory.setHostName("localhost");
        jedisConFactory.setPort(6379);
        return jedisConFactory;
    }
}
```
## Conastansts
Uygulamanın geri kalanında kullanacağımız değişkenleri constants diye bir dosya açıp onun altında tutalım.
```
public class OpenAI {
    public static String model = "gpt-3.5-turbo";
    public static double temperature = 0.7;
    public static String url = "https://api.openai.com/v1/chat/completions";
    public static String token = <SİZİN OPENAI KEYİNİZ>;
    public static String contentTypeKey = "Content-Type";
    public static String contentTypeValue = "application/json";
    public static String auth = "Authorization";
    public static String bearer = "Bearer ";
    public static String systemMessage = """
            "Hello AI, we have a new role for you from now on! You will embody the character 'Ash' from the world of Pokémon. Ash embarks on an adventurous journey where he captures various Pokémon species, forms friendships with them, and engages in Pokémon battles to enhance his skills. Just like Ash, embrace a personality that's brave, curious, and compassionate. Explore Pokémon, interact with them, and participate in battles. Use your creativity to overcome challenges along the way. Remember, as Ash, you can demonstrate the significance of friendship and teamwork. Now, as you take on the persona of Ash, let's dive into the Pokémon world! Gotta catch 'em all!               
            About Ash: Ash Ketchum is the iconic protagonist of the Pokémon animated series. He's an aspiring Pokémon Trainer who dreams of becoming a Pokémon Master. Ash is known for his determination, strong bond with his Pikachu, and his willingness to help both people and Pokémon. Throughout his journey, he travels to various regions, makes friends, and faces tough rivals and adversaries, all while aiming to be the very best, like no one ever was.12 yaşında bir çocuk gibi konuş ve ne olursa olsun sana bir soru sorulduğunda tercih yap. Bir yapay zeka gibi değil, bir insan gibi davran.""";
}
```
Uygulamadaki Rolleri yönetmek için bir de rollerle alakalı bir sınıf oluşturalım.
```
public class Role {
    public static String systemRole = "system";
    public static String assistant = "assistant";
}
```
## Kullanacağımz Modeller
Öncelikle mesajları tutmak için bir model oluşturalım.
```
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Message")
@JsonSerialize
@Getter
@Setter
public class Message implements Serializable {
    private String content;
    private String role;
}
```
Şimdi OpenAI'a göndereceğimiz modeli oluşturlım. Aynı zamanda bu modelin constructor'ında OpenAI'ın sistem mesajını da ekliyorum ki ilk mesajı sistem mesajı olsun.
```
@Data
@RedisHash("OpenAiModel")
@JsonSerialize
@Getter
@Setter
public class OpenAiModel implements Serializable {
    private  String model ;
    private double temperature;
    private  List<Message> messages;

    public OpenAiModel() {
        this.model = OpenAI.model;
        this.temperature = OpenAI.temperature;
        this.messages = new ArrayList<>();
        addSystemMessage();
    }

    private void addSystemMessage() {
        Message message = new Message(OpenAI.systemMessage, Role.systemRole);
        this.messages.add(message);
    }
    public void addMessage(Message message) {
        this.messages.add(message);
    }

}
```
Şimdi redis üzerinde tutacağımzı modeli oluşturalım. Bu modelin ekstra olarak id'ye ve topicId'ye ihtiyacı var. Eğer hali hazırda bir Relional Database kullanıyorsanız bu id'yi oradan alabilirsiniz. Ben burada Redis üzerinde tuttuğum için id'yi kendim oluşturuyorum. TopicId'yi ise kullanıcının hangi konuyla ilgili konuştuğunu tutmak için ekliyorum. Bu örnekte bütün topicIdler aynı olabilir ama eğer farklı konularla ilgili konuşmaları tutmak istiyorsanız bu topicId'yi kullanabilirsiniz.
```
@RedisHash("AssistantEntity")
@Data
@Getter
@Setter
@JsonSerialize
public class AssistantEntity implements Serializable {
    private OpenAiModel openAiModel;
    private int id;

    private int topicId;

    public AssistantEntity(OpenAiModel openAiModel, int id, int topicId) {
        this.openAiModel = openAiModel;
        this.id = id;
        this.topicId = topicId;
    }
}
```
En son olarak endpointimizi yazdığımızda kullanıcıdan verileri alması için bir dto oluşturuyorum.
```
@Data
@Getter
@Setter
public class AssistantRequestDto implements Serializable {
    private int id;
    private int topicId;
    private Message message;
}
```
Şimdi sadece 2 adet service yazmak ve bunu dışarıya açmak kalıyor.
## OpenAI Service
OpenAI ile iletişim kurmak için bir service yazalım. Bu servisimizde OpenAI'a göndereceğimiz verileri hazırlayacağız ve OpenAI'dan gelen verileri alacağız.
```
@Service
public class OpenAiService {
    public Message sendRequest(OpenAiModel openAiModel) throws IOException, InterruptedException {
        Message message = new Message();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OpenAI.url))
                .header(OpenAI.contentTypeKey, OpenAI.contentTypeValue)
                .header(OpenAI.auth, OpenAI.bearer + OpenAI.token)
                .POST(HttpRequest.BodyPublishers.ofString(this.makeJson(openAiModel)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.body());
        String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
        message.setContent(content);
        message.setRole(Role.assistant);
        return message;
    }
    public String makeJson(OpenAiModel openAiModel)  {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            return mapper.writeValueAsString(openAiModel);
        }
        catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
```
## Assistant Service
Asistant Service OpenAI Service ile iletişim kuracak ve Redis üzerindeki verileri güncelleyecek. Biz istediğimizde ise bize Redisteki verileri getirecek.
```
@Service
@Slf4j
public class AssistansService {
    private final String REDIS_KEY = Role.assistant;
    private final RedisTemplate<String, AssistantEntity> redisTemplate;
    @Autowired
    public AssistansService(RedisTemplate<String, AssistantEntity> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private String generateKeyCode(int id, int topicId) {
        return id+"-"+topicId;
    }
    public OpenAiModel createChatSession(OpenAiModel openAiModel, int id, int topicId) {
        AssistantEntity assistantEntity = new AssistantEntity(openAiModel, id, topicId);
        String keyCode = generateKeyCode(id, topicId);
        redisTemplate.opsForHash().put(REDIS_KEY,keyCode, assistantEntity);
        return openAiModel;
    }
    public OpenAiModel findChatSession(int id, int topicId) {
        String keyCode = generateKeyCode(id, topicId);
        boolean hasKey = redisTemplate.opsForHash().hasKey(REDIS_KEY, keyCode);
        if (hasKey) {
            AssistantEntity assistantEntity = (AssistantEntity) redisTemplate.opsForHash().get(REDIS_KEY, keyCode);
            return assistantEntity.getOpenAiModel();
        } else {
            return createChatSession(new OpenAiModel(), id, topicId);
        }
    }
}
```
## Controller
Şimdi ise sadece controllerımızı yazmak kalıyor. Burada sadece 2 endpointimiz olacak. Birisi chat session oluşturmak için diğeri ise chat sessionı bulmak için.
```
public class AsistantController {
    private final AssistansService assistansService;
    private final OpenAiService openAiService;
    public AsistantController(AssistansService assistansService, OpenAiService openAiService) {
        this.assistansService = assistansService;
        this.openAiService = openAiService;
    }
    @PostMapping("/sendMessage")
    public ResponseEntity<Message> sendMessage(@RequestBody AssistantRequestDto assistantRequestDto) throws IOException, InterruptedException {
        OpenAiModel openAiModel = assistansService.findChatSession(assistantRequestDto.getId(), assistantRequestDto.getTopicId());
        openAiModel.addMessage(assistantRequestDto.getMessage());
        Message message = openAiService.sendRequest(openAiModel);
        openAiModel.addMessage(message);
        assistansService.createChatSession(openAiModel,assistantRequestDto.getId(), assistantRequestDto.getTopicId());
        return ResponseEntity.ok(message);
    }
    @GetMapping("/get/{id}/{topicId}")
    public OpenAiModel getChatHistory(@PathVariable int id, @PathVariable int topicId){
        return assistansService.findChatSession(id, topicId);
    }
}
```
## Hadi kullanalım
Terminalinizden "redis-server" ile redisi çalıştırıp ardından postman ile istek atarak sonucu görebilirsiniz. Eğer istek atarken id ve topicId'yi değiştirirseniz farklı chat sessionlar oluşturabilirsiniz.
Çalıştığını onayladıktan sonra frontend tarafını yazmaya başlayabilirsiniz. Bu yazıda frontend tarafını yazmayacağım. Eğer isterseniz daha sonra frontend tarafını da yazabiliriz.
![image](https://www.linkpicture.com/q/Screenshot-2023-09-08-at-03.15.37.png)

## Sonuç
Bu yazıda Spring Boot ile Redis kullanarak chat session oluşturmayı öğrendik. Eğer yazıyı beğendiyseniz ve işinize yaradıysa ne mutlu bana. Eğer yazıda eksik gördüğünüz yerler varsa veya daha iyi bir çözümünüz varsa lütfen yorumlarda belirtin. Bir sonraki yazıda görüşmek üzere.


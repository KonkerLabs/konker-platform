# Guia de Uso da Plataforma Konker

___Unfortunately this doc is only available in pt_BR for the moment, other languages may be available soon___

__NOTE__ que essa documentacao pode/esta desatualizada, apesar de ainda ser coesa no que tange o passo a passo e ser valida como um guia introdutorio, algumas funcoes e layouts podem divergir dependendo da versao da plataforma que voce esteja executando. 


## Índice <a name="table-of-contents">
- [Indice](#table-of-contents)
- [Porque usar uma Plataforma](#why-konker-platform)
- [Conceitos](#concept)
  - [Dispositivo](#device)
  - [Canal](#channel)
  - [Rota](#route)
  - [Transformaçao](#transformation)
- [Como usar](#usage)
  - [Registrando o seu dispositivo](#registring-a-device)
  - [Enviando dados](#sending-events)
    - [Envio via REST](#sending-events-with-http-res)
      - [Envio REST via linha de comando com cURL](#sending-events-with-http-rest-curl)
      - [Envio REST sem autenticação básica](#sending-events-with-http-rest-basicauth)
    - [Envio via MQTT](#sending-events-with-mqtt)
      - [Envio MQTT via Python](#sending-events-with-mqtt-python)
      - [Envio MQTT via Javascript](#sending-events-with-mqtt-javascript)
    - [(Opcional) Enviando dados com Geolocalização](#sending-events-with-geolocation)
    - [(Opcional) Enviando dados com timestamp de criação](#seding-events-with-creation-timestamp)
  - [Recebendo Dados](#receiving-events)
    - [Recebendo os dados via REST](#receiving-events-with-http-REST)
    - [Recebendo dados via REST Polling usando cURL](#receiving-events-with-http-REST-pooling-curl)
    - [Recebendo dados via REST Polling usando PHP](#receiving-events-with-http-REST-pooling-php)
    - [Recebendo dados via REST no browser, através de JSONP](#receiving-events-with-http-REST-browser-jsonp)
    - [Recebendo dados via REST Polling de forma paginada, usando cURL](#receiving-events-with-http-REST-pooling-curl-pagination)
    - [Recebendo dados via REST Long Polling de forma paginada, usando cURL](#receiving-events-with-http-REST-long-pooling-curl)
  - [Recebendo dados via MQTT](#receiving-events-with-mqtt)
    - [Recebendo via MQTT em Python](#receiving-events-with-mqtt-python)
  - [Visualizando os Dados](#data-visualization)
  - [Adicionando Comportamento Personalizado](#adding-custom-behavior)
    - [Enviando dados de um dispositivo para outro através de Rotas](#sending-data-from-a-device-through-routes)
  - [Administrando suas configurações via Rest](#device-management-with-rest)
    - [Testando e conhecendo os Endpoints via SwaggerDocs](#exploring-the-api-with-swagger)
  - [Usando um Gateway de dados](#using-a-gateway)
    - [Enviando mensagens de um Dispositivo através de um Gateway](#sending-events-from-a-device-through-a-gateway)
    - [Enviando mensagens via Gateway sem a necessidade do dispositivo já existir](#sending-events-through-a-gateway-with-a-new-device)


## Porque usar uma Plataforma? <a name="why-konker-platform"></a>
Construir soluções de IoT que resolvem problemas do mundo real não é uma tarefá trivial. Em geral, é necessária uma combinação de habilidades:

- OT, ou Operational Technology. Trata-se de entender o problema real que se está tentando resolver e como resolvê-lo no mundo real. Se a solução é uma solução de irrigação na agricultura, é necessário conhecer quais os problemas associados, quais os equipamentos são usados para irrigar, de que forma trabalham, onde estão suas limitações, e assim por diante.
- HW, ou Hardware. Trata-se de construir o equipamento que trabalha como ponte entre OT e a Internet. Esse equipamento, dependendo das condições de trabalho, talvez tenha que ser resistente a humidade, sujeira, vibração, temperatura, conectividade. entre outros aspectos importantes para uma instalação em campo.
- SW, ou Software. Trata-se do I (Internet) em "Internet das Coisas". Construir uma solução de software na Internet capaz de receber, armazenar, processar e analisar grandes volumes de dados, com alta disponibilidade, segurança e desempenho. Gerenciar os dispositivos.

A Plataforma Konker é uma ferramenta que endereça o último ponto desta lista: o Software na Internet. Trata-se de uma Plataforma na nuvem num modelo PaaS (Platform as a Service) que realiza a coleta de dados e a comunicação entre os dispositivos. O objetivo é que o criador de uma solução possa concentrar seus esforços pensando na solução em si, no hardware e no problema que deve ser resolvido - em lugar de desperdiçar tempo realizando setup de servidores, replicação, backup, monitoração e assim por diante. A prototipação de soluções é especialmente simples: em alguns minutos é possível começar a enviar e visualizar dados.

## Conceitos <a name="concept"></a>
Para usar a Plataforma Konker, alguns conceitos são importantes:

### Dispositivo <a name="device"></a>
Um dispositivo na Pataforma Konker é uma representação de um dispositivo físico (ou virtual) que envia ou recebe dados. É o equivalente a "Things" quando se fala em "Internet of Things". Apenas dispositivos cadastrados na plataforma podem enviar e receber dados.

### Canal <a name="channel"></a>
O conceito de canal está associado ao dispositivo e é uma maneira de agrupar mensagens com conteúdo semelhante para que possam ser tratadas em conjunto. Um exemplo é o caso em que um dispositivo tem 2 sensores: um sensor de umidade e um sensor de temperatura. Nesse caso, é aconselhável que os dados enviados pelo dispositivo sejam enviados para um canal "temperatura" e os dados de umidade para um canal de "umidade". Dessa forma, pode-se aplicar filtros ou visualizações apenas pra um tipo de informação ou de outra. Canais não precisam ser cadastrados: basta informar o nome do canal durante o envio da informação que ele será automaticamente criado a partir daquele momento.
Os canais também são relevantes para o dispositivo que recebe os dados. Suponha que um dispositivo receba da plataforma 2 tipos de informação, como por exemplo comandos e configurações. Nesse caso, o dispositivo pode fazer uma assinatura para receber apenas os dados de comandos ou apenas os dados de configuração. Os nomes de canais podem ser definidos com os caracteres de A a Z maiúsculos ou minúsculos, numerais de 0 a 9 e os caracteres especiais '-' e '_'. Espaços não são permitidos.

### Rota <a name="route"></a>
Uma rota é um caminho que liga um dispositivo de entrado a um dispositivo (ou webservice) de saída. Dessa forma, se há um dispositivo que contém um botão chamado "Botao 01" e um dispositivo que contém um LED chamado "LED 01", pode-se criar uma rota que liga o dispositivo "Botao 01" ao dispositivo "LED 01", de forma que as mensagens enviadas ao pressionar o botao sejam encaminhadas ao dispositivo que acende ou apaga o LED. Nesse cenário, também seria necessário especificar na rota qual o canal em que os dispositivos se comunicam.

### Transformação <a name="transformation"></a>
Um ponto de transformação permite que uma mensagem seja arbitrariamente manipulada durante o processamento da rota para que ela esteja no formato que o dispositivo de saída possa entender. Isso é realizado através de chamadas a serviços externos à plataforma que devem receber as mensagens, processá-las e devolvê-las à plataforma para continuar o processamento. Ver "Adicionando Comportamento Personalizado" para mais informações.

## Como Usar <a name="usage"></a>
### Registrando seu Dispositivo <a name="registring-a-device"></a>
Antes de começar a enviar dados, o dispositivo precisa ser registrado na Plataforma Konker. O processo de registro é simples: a partir do Menu Principal, na tela de Dispositivos, basta adicionar um Novo Dispositivo.

![botao](./guia-dispositivos-0.png)

![novo](./guia-dispositivos-1.png)

O cadastro é muito simples e rápido, basta informar:

![form](./guia-dispositivos-2.webp)


- Device ID: um ID único para aquele dispositivo. Como número serial, um identificador sequencial, qualquer informação que possa identificar unicamente aquele dispositvo;
- Device Name: um nome amigável para o dispositivo.
- Descrição: um texto mais longo do que é o dispositivo, o que ele faz e assim por diante;
- Ativo: se esse dispositivo pode enviar ou receber dados. 
Dispositivos inativos não podem enviar ou receber dados.

Após registrar o dispositivo, é necessário solicitar as credenciais de acesso. Tais credenciais são usadas para que o dispositivo faça a autenticação. Trata-se de um usuário e uma senha que estão associados de forma intransferível àquele dispositivo. Se um usuário tem 10 dispositivos cadastrados, cada um desses dispositivo terão um usuário de dispositivo e uma senha de dispositivo associados. A geração das credenciais estão no botão Conectar:

![conect](./guia-dispositivos-3.png)

Clicando no botão "Gerar Senha", uma nova senha será gerada para aquele dispositivo. O usuário está no campo "Usuário" destacado.

![form](./guia-dispositivos-5.webp)

IMPORTANTE: Quando a senha for gerada, copie e salve tanto o usuario quanto a senha em um local seguro. A Plataforma Konker não armazena essa senha por questões de segurança (armazena apenas o hash criptográfico) e portanto não é possível recuperá-la. Caso a senha seja perdida, uma nova senha deverá ser gerada.

![form](./guia-dispositivos-6.webp)

Nesse exemplo específico, sempre que  o dispositivo precisar enviar dados (HTTP ou MQTT), deve usar como username bhh8ih0tj8di e senha 1Vab8FyqFro4. 
Para os seus dispositivos, usuário e senha serão diferentes.

## Enviando dados <a name="sending-events"></a>
É possível enviar dados para a Plataforma Konker através de HTTP / REST ou via MQTT. O comportamento é ligeiramente diferente, mas ambas são alternativas aceitáveis para o envio de informações.

## Envio via REST <a name="sending-events-with-http-res"></a>

Para enviar os dados via REST, as informações precisam ser um documento JSON válido. Ao registrar o dispositivo e criar as credenciais de acesso, o dispositivo passa a ter um usuário e senha próprios que deverão ser usados no momento de enviar os dados. Os dados devem ser enviados via POST com o cabeçalho de 'Content-Type: application/json' e autorização do tipo Http-Basic, com o usuário e senha informados.
__IMPORTANTE:__ um device só está autorizado a enviar dados para um canal associado àquele device específico. Não é possível autenticar-se com as credenciais de um device A e enviar dados para um canal do device B. Caso seja esse o desejo, deve-se usar uma rota do device A para o device B.
Suponha que o dispositivo já tenha sido cadastrado e que as credenciais de acesso já tenham sido geradas, e que o dispositivo deseja enviar a mensagem abaixo
  - Mensagem a ser enviada: {"temperature": 22, "unit": "celsius"}
  - Usuário de dispositivo gerado pela plataforma: gr4ouo53timc
  - Senha de dispositivo gerada pela plataforma: mflaCCWx9jSh
  - URL de envio (obtida na tela de acesso): `http://data.konkerlabs.net/pub/gr4ouo53timc/{canal}`
Nesse caso, vamos enviar os dados para um canal chamado "temperature", para agregar informações relacionadas com temperatura. Alguns exemplos de como realizar o envio via REST:

### Envio REST via linha de comando com cURL: <a name="sending-events-with-curl"></a>

```
curl -u 
bhh8ih0tj8di:1Vab8FyqFro4 -X POST -H 'Content-Type: application/json' \
    -d '{"temperature": 22, "unit": "celsius"}' \
     http://data.demo.konkerlabs.net/pub/bhh8ih0tj8di/temperature 
```

__Retorno__ (HTTP Status=200): `{"code":"200","message":"OK"}`


### Envio REST sem autenticação básica: <a name="sending-events-with-http-res-basicauth"></a>
Caso o cliente REST não tenha suporte para autenticação básica é possível passar o usuário e senha do dispositivo por parâmetros na URL. Porém, desencorajamos essa forma de autenticação, pois URLs são armazenadas em log de servidores, podem ficar guardadas no histórico do navegador e são passadas como Referrer headers. Tudo isso pode expor o usuário e senha do dispositivo.
  - deviceKey: usuário do dispositivo
  - deviceSecret: senha  do dispostivo
Exemplo: `http://data.demo.konkerlabs.net/pub/bhh8ih0tj8di/temperature?deviceKey=bhh8ih0tj8di&deviceSecret=1Vab8FyqFro4`


### Envio via MQTT <a name="sending-events-with-mqtt"></a>

Para enviar os dados via MQTT, as informações precisam ser um documento JSON válido. Ao registrar o dispositivo e criar as credenciais de acesso no portal de administração da Plataforma, o dispositivo passa a ter usuário e senha próprios que deverão ser usados no momento de enviar os dados. O usuário e senha deverão ser usados para se conectar via MQTT e publicar as informação numa esturuta de tópicos específica:

`pub/<device_username>/<canal>`

Suponha que o dispositivo já tenha sido cadastrado e que as credenciais de acesso já tenham sido geradas, e que o dispositivo deseja enviar a mensagem abaixo
  - Mensagem a ser enviada: {"temperature": 22, "unit": "celsius"}
  - Usuário de dispositivo gerado pela plataforma: gr4ouo53timc
  - Senha de dispositivo gerada pela plataforma: mflaCCWx9jSh
  - Endereço de envio (obtido na tela de acesso): mqtt://mqtt.demo.konkerlabs.net:1883
  - Tópico de envio (obtida na tela de acesso):  data/gr4ouo53timc/pub/{canal}

Nesse caso, vamos enviar os dados para um canal chamado "temperature", para agregar informações relacionadas com temperatura. Alguns exemplos de como realizar o envio:

### Envio MQTT via Python: <a name="sending-events-with-mqtt-python"></a>

```
import paho.mqtt.client as mqtt
import json

client = mqtt.Client()
client.username_pw_set("gr4ouo53timc", "mflaCCWx9jSh")
client.connect("mqtt.demo.konkerlabs.net", 1883)
client.publish(
  "data/gr4ouo53timc/pub/temperature", 
  json.dumps({"temperature": 22, "unit": "celsius"}))
```

__IMPORTANTE:__ um device só está autorizado a enviar dados para um canal daquele device específico. Não é possível autenticar-se com as credenciais de um device A e enviar dados para um canal do device B. Caso seja esse o desejo, deve-se usar uma rota do device A para o device B.

__IMPORTANTE:__ A plataforma Konker suporta hoje MQTT com QOS=0, ou seja: não há garantias transacionais de entrega das mensagens caso o ambiente esteja sob carga elevada, por exemplo. Dessa forma, MQTT é o cenário ideal para envio de dados como sensores e streaming de dados. Caso seja necessário garantir que a mensagem tenha sido entregue e processada pela plataforma, o o serviço REST é recomendado.

__IMPORTANTE:__ As mensagens enviadas via MQTT são limitadas a 32Kb. Caso a mensagem ultrapasse esse limite, ela será rejeitada pelo broker.

### Envio MQTT via Javascript: <a name="sending-events-with-mqtt-javascript"></a>

para enviar dados para a plataforma via Javascript, em geral aplicações que rodam como servidores (Node.js) ou cliente (dentro do browser) podem conectar-se diretamente a plataforma usando MQTT.
Vale ressaltar que há duas opções ao utilizar  bibliotecas MQTT em Javascript: as que falam TCP nativamente ou outras que utilizam Websockets. 

A plataforma da Konker na nuvem suporta conexões TCP diretamente – portanto soluções usando Websocket precisam utilizar um proxy para poder funcionar adequadamente.
vamos ver as diferenças entre elas.
  - usando a bilbiioteca mqtt (TCP nativo) ... https://www.npmjs.com/package/mqtt
<TODO>
  - usando a biblioteca Paho MQTT (websocket) https://www.eclipse.org/paho/clients/js/

Nesse caso é necessário rodar um proxy websocket – um simples e rápido de executar é o websocketify, (https://hub.docker.com/r/efrecon/websockify) que pode ser instanciado via Docker da seguinte maneira:

```
docker run -it --rm -p 8080:80 --name websockify efrecon/websockify 80 mqtt.demo.konkerlabs.net:1883
```

Dessa forma ele criará um proxy para o cliente websocket se conectar via a plataforma de forma transparente, recebendo conexões em `127.0.0.1:8080` e redirecionando para o endereço `mqtt.demo.konkerlabs.net:1883`.

__NOTA:__ se você for rodar esse websocket em produção é recomendável que utilize um servidor (DigitalOcean, AWS, Azure, ...) rodando o websocketify, com opções de segurança ativa (filtros, etc) ara permitir sua conexão. 
feito isso é necessário aplicar um patch no código fonte-original do client do paho-mqtt para que ele funcione adequadamente com o webproxy – de forma com que ele consiga estabelecer conexões de protocolo binário (e não apenas mqtt ou mqttv1.3 que é o  seu padrão) 1 conforme descrito abaixo... o código fonte original da biblioteca pode ser encontrado em https://www.eclipse.org/paho/clients/js/ nessa versão 1.1.0 ... pode-ser aplicar o seguinte patch .

(...) na linha 1050 há a definição do protocolo a ser utilizado pela biblioteca ... usar o binario para funcionar adequadamente com o websocket proxy 
var wsType = 'binary';
```
if (this.connectOptions.mqttVersion < 4) {
  this.socket = new WebSocket(wsurl, [wsType]);// ["mqttv3.1"]);
} else {
  this.socket = new WebSocket(wsurl, [wsType]);// ["mqtt"]);
}
```
(...) feito isso basta utilizar a classe Paho.Client (antiga Paho.MQTT.Client) para estabelecer a conexão e fazer o envio dos dados ... 

__NOTA1:__ é necessário que o payload enviado seja um JSON serializável ... não fazer dessa forma implica em que a mensagem não será ingerida na plataforma adequadamente.

__NOTA2:__ é necessário utilizar essa bilbioteca alterada para que a conexão funcione ... senão receberá um erro "code 400, message Client must support 'binary' or 'base64' protocol"
feito isso o código abaixo funciona adequdamente – apenas garanta que o servidor onde o Paho.Client se conecta é o mesmo servidor onde você configurou o seu websocketify.

```
<html>
  <head>
   <script src="./paho-mqtt.js" type="text/javascript"></script>
   <script type="text/javascript" language="javascript">

      // Create a client instance
      var client = new Paho.Client('127.0.0.1', 8080, '', "paho sample client");

    // set callback handlers
    client.onConnectionLost = onConnectionLost;
    client.onMessageArrived = onMessageArrived;

    // connect the client
    var username = '<PLACE USERNAME GENERATED BY KONKER PLATFORM FOR THIS DEVICE>';
    var password = '<PASSWORD GENERATED BY KONKER PLATFORM>';
    // TODO-REMOVE THIS
    
    client.connect({userName: username, password: password, onSuccess:onConnect, onFailure:doFail});
    // TO ENABLE TRACING ... 
    // client.startTrace();

    function doFail() {
      console.log('FAIL');
    }

    // called when the client connects
    function onConnect() {
      // Once a connection has been made, make a subscription and send a message.
      console.log("onConnect");
      // subcribe to a channel
      client.subscribe("data/"+username+"/pub/sample");

      // TYPE #1: sending a message using Message object ...
      // send a message using Paho.Message object ... 
      // IMPORTANT: the content of the message should be a VALID JSON ... 
      // most efficient way to create is generate an Object / dictionary / Map and generate a JSON.stringify() from it 
      // * MUST define a topic or destinationName 
      //
      message = new Paho.Message(JSON.stringify({data:new Date()}));
      message.topic = "data/"+username+"/pub/sample";
      //
      client.send(message);

      // client.send(message);
      // TYPE #2: send a message using direct interface ... 
      //
      client.publish("data/"+username+"/pub/sample", JSON.stringify({dt: new Date()}));

      // logging

      console.log('PAHO TRACKING');
      console.log(client.getTraceLog());
    }

    // called when the client loses its connection
    function onConnectionLost(responseObject) {
      if (responseObject.errorCode !== 0) {
        console.log("onConnectionLost:"+responseObject.errorMessage);
      }
    }

    // called when a message arrives
    function onMessageArrived(message) {
      console.log("onMessageArrived:"+message.payloadString);
    }
    </script>
  </head>
  <body>
  </body>
</html>
```

1. há alguns posts relacionados a esse "bug" que podem ser encontrados em https://github.com/eclipse/paho.mqtt.javascript/issues/57

(Opcional) Enviando dados com Geolocalização <a name="sending-events-with-geolocation"></a>

É possível enviar dados de geolocalização tanto via MQTT ou REST, essa informações devem ser enviadas no formato JSON com chaves predefinidas, utilizando o json dos exemplos anteriores, vamos apenas adicionar os campos responsáveis pela informação de geolocalização:
  - Mensagem a ser enviada: {"temperature": 22, "unit": "celsius", "_lat": -23.5746571, "_lon": -46.6910183 , "_hdop": 10, "_elev": 3.66}

(Opcional) Enviando dados com timestamp de criação <a name="sending-events-with-creation-timestamp"></a>

Nós sugerimos fortemente que ao enviar uma mensagem para a plataforma, utilizar a chave _ts no json para informar o timestamp de criação dessa mensagem que será enviada. Esse passo é importante caso futuramente você precise saber o momento exato que essa mensagem foi gerada. Caso esse campo seja otimido, será setado um timestamp no momento que a mensagem for ingerida na plataforma. Abaixo adicionamos mais uma chave no json que utilizamos nos exemplos anteriores:
  - Mensagem a ser enviada: {"temperature": 22, "unit": "celsius", "_lat": -23.5746571, "_lon": -46.6910183 , "_hdop": 10, "_elev": 3.66, "_ts": 1510945593000}
    - a chave "_ts" deve ser um epoch timestamp em milisegundos 

## Recebendo Dados <a name="receiving-events"></a>

### Recebendo os dados via REST

Para receber dados enviados pela plataforma via REST, o dispositivo precisa ter sido previamente cadastrado e ter credenciais geradas (usuário e senha específicos do dispositivo). A autenticação é do tipo Http-Basic.

O recebimento de dados via HTTP pode funcionar em dois modelos semelhantes, mas com características distintas: polling ou long polling. No modelo de polling, o device pode solicitar à Plataforma Konker as últimas mensagens recebidas. Caso não haja mensagens recebidas recentemente, a plataforma retorna uma lista vazia. No modelo de long polling, o device também solicita as mensagens mais recentes, mas informa também um tempo de espera. Caso não haja mensagens a serem retornadas, a Plataforma Konker irá esperar pelo tempo informado até que uma nova mensagem seja retornada.

O método HTTP usado em ambos os casos é o GET e dois parâmetros opcionais podem ser informados:
  - waitTime: tempo de espera em milissegundos, caso não haja mensagens a serem retornadas. Máximo de 30000.
  - offset: é um timestamp em UTC - número de milissegundos de 01/01/1970. Apenas mensagens que chegaram após esse timestamp serão retornadas

Caso não sejam informados waitTime ou offset, a última mensagem disponível será retornada. Caso seja informado um offset, apenas mensagens recebidas após esse timestamp serão retornadas. Em qualquer caso, o número máximo de mensagens retornadas é no máximo 100.

Além da mensagem em si, este serviço também retorna informações de metadados associados a esta mensagem. Um exemplo de retorno segue abaixo:

```
[
  {
    "meta": {
      "timestamp": 1476100297217,
      "incoming": {
        "deviceId": "sensorA",
        "channel": "temperature"
      },
      "outgoing": {
        "deviceId": "actuatorB",
        "channel": "out"
      }
    },
    "data": {
      "temperature": 21,
      "unit": "celsius"
    }
  },
  {
    "meta": {
      "timestamp": 1476102464535,
      "incoming": {
        "deviceId": "sensorA",
        "channel": "temperature"
      },
      "outgoing": {
        "deviceId": "actuatorB",
        "channel": "out"
      }
    },
    "data": {
      "temperature": 23,
      "unit": "celsius"
    }
  }
]
```

O retorno é uma lista de documentos. Cada um dos documentos, contém duas chave principais:
  - "meta": metainformação relacionada com aquela mensagem. São dados que não foram enviados por dispositivos, mas são informações de controle, tais como quando a   mensagem foi recebida, qual o dispositivo e canal que originaram a mensagem, qual o dispositivo e canal de destino da mensagem.
  - "data": o conteúdo da mensagem enviada pelo dispositivo, incluindo possíveis alterações que tenham ocorrido durante a execução da rota, tais como transformações.

__IMPORTANTE:__ um device pode apenas receber dados de canais associados a ele. Caso seja necessário receber dados associados a outro dispositivo, uma rota deve realizar esse trabalho.

Abaixo, alguns exemplo de uso.
Suponha que o dispositivo já tenha sido cadastrado e que as credenciais de acesso já tenham sido geradas. Desejamos receber os dados que foram destinados ao dispositivo em seu canal "out".
  - Usuário de dispositivo gerado pela plataforma: gr4ouo53timc
  - Senha de dispositivo gerada pela plataforma: mflaCCWx9jSh
  - Endereço de recebimento (obtido na tela de acesso): `http://api.konkerlabs.net:80/sub/gr4ouo53timc/out`

### Recebendo dados via REST Polling usando cURL

```
curl -u gr4ouo53timc:1Vab8FyqFro4 http://data.konkerlabs.net/sub/gr4ouo53timc/out
```

__Retorno:__(HTTP Status=__200__) 
```
[{"meta":{"timestamp":1476495431147,
"incoming":{"deviceId":"MeuDispositivo01","channel":"temperature"},
"outgoing":{"deviceId":"MeuDispositivo01","channel":"out"}},
"data":{"unit":"celsius","temperature":22}}]
```

### Recebendo dados via REST Polling usando PHP

O mesmo exemplo acima em PHP:

```
<?php
  $username="gr4ouo53timc";
  $password="1Vab8FyqFro4";
  $channel="out";

  $url="http://data.konkerlabs.net/sub/$username/$channel";
  $ch = curl_init();
  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
  curl_setopt($ch, CURLOPT_USERPWD, "$username:$password");
  curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
  $output = curl_exec($ch);
  $decoded = json_decode($output);
  curl_close($ch);
  print_r($decoded);
?>
```

### Recebendo dados via REST no browser, através de JSONP

O mesmo exemplo acima pode ser implementado diretamente no browser, usando JSONP.
A URL usada tem o seguinte formato:
`http://<usuario>:<senha>@data.konkerlabs.net/sub/<usuario>/<canal>`
Nesse caso, o usuário e senha devem fazer parte da URL, como no exemplo abaixo:
```
$.ajax({
  url: "http://gr4ouo53timc:1Vab8FyqFro4@api.konkerlabs.net/sub/gr4ouo53timc/out",
  type: "GET",
  dataType: 'jsonp',
  success: function(data, textStatus, XMLHttpRequest) {
      // os dados são retornados no campo data.
      console.log(JSON.stringify(data))
  },
  error: function(XMLHttpRequest, textStatus, errorThrown) {
      console.log("Error!")
  }
});

```

É necessário incluir o jQuery para executar esse exemplo.

### Recebendo dados via REST Polling de forma paginada, usando cURL

Nesse caso, basta usar o timestamp da última mensagem recebida no passo anterior. Por exemplo: recebendo os dados mais recentes, que chegaram depois do exemplo anterior (usando portanto o filtro de timestamp > 1476495431147).
```
curl -u gr4ouo53timc:1Vab8FyqFro4 http://data.demo.konkerlabs.net/sub/gr4ouo53timc/out?offset=1476495431147
```
__Retorno:__(HTTP Status=200) 
```
[
  {
    "meta":
    {
      "timestamp":1476495678084,
      "incoming":{
        "deviceId":"MeuDispositivo01",
        "channel":"temperature"
      },
      "outgoing":{
        "deviceId":"MeuDispositivo01",
        "channel":"out"
      }
    },
    "data":{
      "unit":"celsius",
      "temperature":22
    }
  },
  {
    "meta":{
      "timestamp":1476495679381,
      "incoming":{
        "deviceId":"MeuDispositivo01",
        "channel":"temperature"
      },
      "outgoing":{
        "deviceId":"MeuDispositivo01",
        "channel":"out"
      }
    },
    "data":{
      "unit":"celsius",
      "temperature":22
    }
  }
]
```

Veja que nesse caso, a mensagem do exemplo anterior não foi retornada.
Esse processo de usar o timestamp da última mensagem reecebida no polling como ofsset da próxima pode ser usada para paginação. As páginas tem tamanho de 100 mensagens.

### Recebendo dados via REST Long Polling de forma paginada, usando cURL

Se realizarmos a mesma operação do passo anterior, usando o timestamp da última mensagem como offset na próxima chamada, eventualmente não haverá mais mensagens disponíveis. Quando isso acontecer, ele retornará uma lista vazia:
```
curl -u gr4ouo53timc:1Vab8FyqFro4 http://data.konkerlabs.net/sub/gr4ouo53timc/out?offset=1476495679381
```
__Retorno:__(HTTP Status=200) 
```
[ ]
```

Se quisermos que o servidor fique bloqueado esperando uma nova mensagem num modelo de leitura bloqueante, podemos passar o parâmetro waitTime em millisegundos.
```
curl -u gr4ouo53timc:1Vab8FyqFro4 http://data.konkerlabs.net/sub/gr4ouo53timc/out?offset=1476495679381&waitTime=30000
```
Nesse caso, o servidor esperará por até 30s aguardando que alguma mensagens esteja disponível para o dispositivo.

## Recebendo dados via MQTT

Para receber os dados via MQTT, basta realizar um subscribe em um tópico específico relacionado ao canal que se deseja escutar. É necessário que tenham sido previamente geradas as credenciais específicas para o dispositivo: username e senha de dispositivo.

A estrutura de canais que se deve fazer o subscribe é a seguinte:
`sub/<device_username>/<canal>`

Abaixo alguns exemplo de recebimento de mensagens via MQTT.

### Recebendo via MQTT em Python

```
import paho.mqtt.client as mqtt
import json

def on_connect(client, userdata, flags, rc):
    print("Connected!")
    client.subscribe("sub/gr4ouo53timc/out")

def on_message(client, data, msg):
    print(msg.topic + " " + str(msg.payload))


client = mqtt.Client()
client.on_message = on_message
client.on_connect = on_connect
client.username_pw_set("gr4ouo53timc", "mflaCCWx9jSh")
client.connect("mqtt.konkerlabs.net", 1883)
client.loop_forever()
```

## Visualizando os Dados

A Plataforma Konker permite visualização rápida de dados que tenham sido ingeridos tanto via MQTT quanto via REST.

![guia-dispositivos-7](./guia-dispositivos-7.png)

A visualização pode ser realizada de duas formas:
- Tabular. Nesse caso, as últimas 100 mensagens ingeridas de um dispositivo ou entregues ao dispositivo pela plataforma estão disponíveis para consulta. Essa opção está disponível através do botão "Eventos" na tela de dispositivos.

- Gráfica. Nesse caso, é possível visualizar graficamente uma série temporal. É um gráfico de linha, onde o eixo X representa o tempo e o eixo Y representa um campo numérico, à sua escolha. Essa opão está disponível no menu principal, através do item "Visualização". Esta tela está limitada a exibir, no máximo, 100 pontos por vez. Apenas dados numéricos que tenham sido ingeridos serão exibidos nessa tela.

### Adicionando Comportamento Personalizado

É possível adicionar comportamento mais sofisticado à Plataforma através dos conceitos de Rotas, Filtros e Transformação.

### Enviando dados de um dispositivo para outro através de Rotas

A Plataforma Konker permite que os dados enviados por um dispositivo sejam transportados até um outro dispositivo. É o que permite a ligação simples entre eles. Um botão poderia enviar uma mensagem para acionar um LED, por exemplo. O mecanismo usado para isso é a criação de Rotas.

Para criar uma Rota entre dois dispositivos, basta usar o menu "Roteador de Eventos"

![guia-dispositivos-8](./guia-dispositivos-8.png)

A criação de uma rota envolve basicamente definir qual o dispositivo e o canal de origem da mensagem e o dispositivo e canal de destino de mensagem. Múltiplas rotas podem ser criadas para um mesmo dispositivo, de forma que uma mensagem (um clique de botão por exemplo) e um único dispositivo.

Por exemplo, para direcionar as mensagens do canal temperature de um Sensor para o canal cmd de um Atuador.

![guia-dispositivos-9](./guia-dispositivos-9.webp)

## Administrando suas configurações via Rest

É possível administrar suas configurações (Dispositivos, Rotas, Transformações, Destinos Rest) que podem ser acessadas via Dashboard web (https://konkerlabs.net)  através de uma Api Rest.

Para tanto, alguns conceitos são importantes:
  - É necessário ter conhecimento basico referente a Apis Rest.
  -  É necessário ter conhecimento basico referente a authenticação / authorização via Oauth2.
  - É possivel explorar / testar os Endpoints via SwaggerDocs, para tanto basta acessar https://api.konkerlabs.net/v1/swagger-ui.html

Testando e conhecendo os Endpoints via SwaggerDocs:
  - Acesse a url https://api.konkerlabs.net/v1/swagger-ui.htmladmi

![api-1](./api-1.webp)

  - Acesse o Link 'Login', e informe seu usuario / senha de acesso a Dashboard ou diretamente pelo link http://api.konkerlabs.net/v1/oauth/token?grant_type=client_credentials:

![api-2](./api-2.webp)

  - Os endpoints disponíveis estarão todos listados logo abaixo:

![api-3](./api-3.webp)

  - Abra um dos 'Boxes' e clique no Icone de esclamação ( Este icone vermelho, demonstra que a authorização via Oauth é requerida antes de iniciar o uso das apis ):

![api-4](./api-4.webp)

![api-5](./api-5.webp)

  - Após esta ação o Icone ficará Azul, informando que você está authorizado e possui um Token de acesso devidamente registrado no dominio api.demo.konkerlabs.net

### Resources disponíveis:

 - Applications
   Esta api permite Criar, Editar, Listar e Remover as suas aplicações.
   Listando as aplicações:

| Tipo          | URI               | Definição                      | Authorização | Payload | Response |
|:-------------:|:-----------------:|:------------------------------:|:------------:|:-------:|:--------:|
| __GET__       | /v1/applications/ | Recupera a lista de aplicações | Oauth2 (application flow) | | `{ "timestamp": 1493043345, "code": 200, "status": "success", "result": [ { "friendlyName": "Andre", "description": null, "name": "evt7flnJ" } ] }` |
| __POST__       | /v1/applications/{ID_APPLICACAO} | Recupera uma Aplicação | Oauth2 (application flow) | | `{ "timestamp": 1493044826, "code": 200, "status": "success", "result": { "friendlyName": "Andre", "description": null, "name": "evt7flnJ" } }` |

- Device Credentials
- Devices
- Events
- Rest Destinations
- Rest Transformations
- Routes
- Users

## Usando um Gateway de dados

Com um Gateway é possivel gerenciar as mensagens de vários dispositivos, não sendo necessário guardar as credenciais individualmente de cada um deles:
É possivel gerenciar os Gateways pelos endpoints:

| Tipo          | URI               | Definição                      | Authorização | Payload | Response |
|:-------------:|:-----------------:|:------------------------------:|:------------:|:-------:|:--------:|
| __GET__       | /v1/{ID_APLICACAO}/gateways | Recupera a lista de Gateways | Oauth2 (application flow) | | __Code: 200__ `{ "timestamp": 1520617039, "code": 200, "status": "success", "result": [ { "name": "GatewayGPSDev", "description": "Gateway For GPS Dev", "locationName": "default", "active": true, "guid": "238f99d1-ab4c-4cde-9fe2-439b1ad6fbc9" }]}` |
| __POST__       | /v1/{ID_APLICACAO}/gateways | Registra um Gateway | Oauth2 (application flow) | `{"name": "Gateway002","description": "simple","locationName": "default","active": true}` | __Code: 201__ `{ "timestamp": 1520616086, "code": 201, "status": "success", "result": { "name": "Gateway002", "description": "gateway 002", "locationName": "default", "active": true, "guid": "36fd3679-fab0-4dff-ae93-84234893b3e6" } }` |
| __DELETE__       | /v1/{ID_APLICACAO}/gateways/{GATEWAY_GUID} | Remove um Gateway | Oauth2 (application flow) | | __Code: 204__  |
| __PUT__       | /v1/{ID_APLICACAO}/gateways/{GATEWAY_GUID} | Atualiza um Gateway | Oauth2 (application flow) | `{"name": "Gateway002","description": "theupdate","locationName": "default","active": true}` | __Code: 200__ `{ "timestamp": 1520617689, "code": 200, "status": "success", "result": { "access_token": "9a46f5d6-69cd-4b1f-86c8-c0c1d6163148", "token_type": "bearer", "scope": "read write" } }` |

### Enviando mensagens de um Dispositivo através de um Gateway

Para enviar mensagens de dispositivos tirando vantagem de um gateway ( que pode ser um software ou um gateway fisico ), vamos primeiro registrar o nosso Gateway fazendo uma requisição na api da Konker:

![api-6](./api-6.webp) ![api-7](./api-7.webp)

Agora que temos o Gateway devidamente cadastrado precisamos de um token de acesso para conectar à plataforma:

![api-8](./api-8.webp) ![api-9](./api-9.webp)

E agora que temos o Token de acesso precisamos criar um dispositivo no mesmo grupo de aplicação em que criamos o Gateway, este dispositivo representa o dispositivo fisico que vai usar o Gateway em questão para publicar mensagens pela plataforma:

![api-10](./api-10.png) ![api-11](./api-11.png)

Agora tomamos nota do id do dispositivo para enviar a primeira mensagem através do modulo de ingestão de mensagens da plataforma:

| Tipo          | URI               | Definição                      | Authorização | Payload | Response |
|:-------------:|:-----------------:|:------------------------------:|:------------:|:-------:|:--------:|
| __POST__       | https://data.konkerlabs.net/gateway/pub | Publica uma mensagem proveniente de um dispositivo através de um Gateway | Bearer Token | `[{"deviceId": "device4GTW01","channel": "tempsensor","payload": {"temperature": 12}}]` | __Code: 200__ `{"code": "200","message": "OK"}` |

É possível alem de enviar o ponto de dado (neste caso a temperatura atual), enviar dados complementares de geo localização e timestamp de criação do evento:

| Tipo          | URI               | Definição                      | Authorização | Payload | Response |
|:-------------:|:-----------------:|:------------------------------:|:------------:|:-------:|:--------:|
| __POST__       | https://data.konkerlabs.net/gateway/pub | Publica uma mensagem proveniente de um dispositivo através de um Gateway | Bearer Token | `[{"deviceId": "device4GTW01","channel": "tempsensor","payload": {"temperature": 20,"_lon": -46.6910183, "_lat": -23.5746571, "_hdop": 10, "_elev": 3.66,"_ts": 1558035161000}}]` | __Code: 200__ `{"code": "200","message": "OK"}` |

Agora é possível verificar as mensagens pelo console da plataforma:

![console-1](./console-1.png)

### Enviando mensagens via Gateway sem a necessidade do dispositivo já existir

Agora é possível enviar mensagens para a plataforma via Gateway sem a necessidade de criar os dispositivos antes de enviar as mensagens. Com o Gateway já criado e com o token gerado conforme mostrado nos passos basta enviar os dados através do modulo de ingestão da plataforma, conforme mostrado abaixo:
__NOTA:__ Caso o dispositivo não exista, internamente a plataforma cria o dispositivo antes de ingerir as mensagens, caso o mesmo já exista, é feita apenas a ingestão das mensagens.

| Tipo          | URI               | Definição                      | Authorização | Payload | Response |
|:-------------:|:-----------------:|:------------------------------:|:------------:|:-------:|:--------:|
| __POST__       | https://data.konkerlabs.net/gateway/pub | Publica uma mensagem proveniente de um dispositivo através de um Gateway | `Authorization: Bearer`, ` X-Konker-DeviceIdField: imei`, `X-Konker-DeviceNameField: name`, `X-Konker-DeviceChannelField: channel`, `Content-Type: application/json` | `[{"_lon": -46.6910183, "_lat": -23.5746571, "_ts": 1558035161000,"temp": 49,"imei":"temp","name":"Sensor-Temperatura-01","channel":"out"},{"_lon": -46.6910183, "_lat": -23.5746571, "_ts": 1558035161000, "temp": 49,"imei":"temp2","name":"Sensor-Temperatura-02","channel":"out"}]` | __Code: 200__ `{"code": "200","message": "OK"}` |

Único ponto que necessita de explicação são os dois campos indispensáveis que precisa ter no Headers da request, que são:
  - X-Konker-DeviceIdField: campo do array no body que contém o valor que será utilizado como ID do dispositivo, para criação e identificação do mesmo dentro da plataforma
  - X-Konker-DeviceNameField: campo do array no body que contém o valor que será utilizado como nome do dispositivo, para criação e identificação do mesmo dentro da plataforma
  - X-Konker-DeviceChannelField: campo do array no body que contém o valor que identifica o canal no qual as mensagens serão ingeridas

É possível verificar abaixo com qual ID o dispositivo foi criado e em qual canal a mensagem foi ingerida:

![console-2](./console-2.png)

![console-3](./console-3.png)
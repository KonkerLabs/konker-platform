var mqtt    = require('mqtt');
var fs = require ('fs');

var options = {
//  key: fs.readFileSync('5ff2079991-private.pem.key'),
//  cert: fs.readFileSync('5ff2079991-certificate.pem.crt'),
//  ca: [ fs.readFileSync('5ff2079991-certificate.pem.crt') ],
  clientId: 'actor-test-01',
  rejectUnauthorized: false,
  reconnectPeriod: 5000,
  username: '843a8kjb9v8i',
  password: 'ie2JXVic6gPc'
};

var client  = mqtt.connect('mqtt://localhost:1883', options);

 
client.on('connect', function () {
  console.log("Connected!")
  client.subscribe('sub/843a8kjb9v8i/temp');
  //client.subscribe('konker/device/device3/data');
});
 
client.on('message', function (topic, message) {
  // message is Buffer 
  console.log("Received on " + topic + " - " + message.toString());
//  client.end();
});


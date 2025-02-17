var mqtt    = require('mqtt');
var fs = require ('fs');

var options = {
//  key: fs.readFileSync('16548b6b19-private.pem.key'),
 // cert: fs.readFileSync('16548b6b19-certificate.pem.crt'),
//  ca: [ fs.readFileSync('16548b6b19-certificate.pem.crt') ],
  clientId: 'sensor-test-01',
  rejectUnauthorized: false,
  reconnectPeriod: 5000,
  username: 'dmtde7b6ge8r',
  password: 'N2I3Z4I13TrS'
};

var client  = mqtt.connect('mqtt://localhost:1883', options);

var timer = setInterval(function(c) {
    var message = {
       deviceId: 'dmtde7b6ge8r',
       temp: Math.floor((Math.random() * 100)),
       timestamp: new Date().getTime() ,
       randomValue: Math.floor((Math.random() * 100))
    }
    console.log("Will publish " + JSON.stringify(message))
    client.publish('pub/dmtde7b6ge8r/send', JSON.stringify(message));
  }, 5000, client);

client.on('connect', function () {
  console.log("Connected!")
});

client.on('reconnect', function() {
  console.log("reconnecting...")
});

client.on('error', function() {
   console.log("error...")
});

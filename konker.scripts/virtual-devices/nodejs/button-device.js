var mqtt    = require('mqtt');
var fs = require ('fs');

var options = {
//  key: fs.readFileSync('16548b6b19-private.pem.key'),
 // cert: fs.readFileSync('16548b6b19-certificate.pem.crt'),
//  ca: [ fs.readFileSync('16548b6b19-certificate.pem.crt') ],
  clientId: 'sensor-test-01',
  rejectUnauthorized: false,
  reconnectPeriod: 5000,
  username: '<username>',
  password: '<passwowrd>'
};

var client  = mqtt.connect('mqtt://<hostname>:<port>', options);

var timer = setInterval(function(c) {
    var message = {
       deviceId: 'temperature',
       timestamp: new Date().getTime() ,
       randomValue: Math.floor((Math.random() * 100))
    }
    console.log("Will publish " + JSON.stringify(message))
    client.publish('konker/device/device1/command', JSON.stringify(message));
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

set -e
​
mongo <<EOF
db = db.getSiblingDB('registry');
​
db.createUser({
    user: 'konker',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'registry'}]
});
​
db = db.getSiblingDB('logs');
​
db.createUser({
    user: 'logs',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'logs'}]
});
​
db = db.getSiblingDB('billing');
​
db.createUser({
    user: 'konker',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'billing'}]
});

db = db.getSiblingDB('private-storage');
​
db.createUser({
    user: 'privatestorage',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'private-storage'}]
});
​
EOF

#!/bin/sh

set -e

echo ""
echo ""
echo "##################################### Konker Open Platform #######################################"
echo "##                                       Version: 0.9.1                                         ##"
echo "##                                  Release date: 2019-01-17                                    ##"
echo "##              Licence: Apache V2 (http://www.apache.org/licenses/LICENSE-2.0)                 ##"
echo "##                           Need Support?: support@konkerlabs.com                              ##"
echo "##################################################################################################"
echo ""
echo ""
echo "hhhhhhhhhhhhhhhhhhhhhyyyyyys/' "
echo "hhhhhhhhhhhhhhhhhhhyyyyyys+.  "
echo "hhhhhhhhhhhhhhhhhhyyyyyyo-   "
echo "hhhhhhhhhhhhhhhhyyyyyys:'    +hy                         sho"
echo "hhhhhhhhhhhhhhyyyyyys+'      +hy '/o+'./oooo/. .oo:+os+- sho '+o/'-+ooo+- -o/:os"
echo "hhhhhhhhhhhhhyyyyyyo-        +hy.sh+'-yh/..:yh/-hho..+hh'sho-yh/'/hs---yh::hho:-"
echo "hhhhhhhhhhhhyyyyyyo'         +hyyh+  ohy    ohy-hh.  -hh.shyhh/  yhsoooss+:hh'"
echo "hhhhhhhhhdddhyyyyyhs-        +hy.sho.-yh/..:hh/-hh'  -hh.sho-yh+./hy-..-:':hh"
echo "hhhhhhhdddddddhyhhhhy+.      :o+  :o+-./oooo/. .oo'  .oo'/o: '/o+.-+oooo+ -oo"
echo "hhhhhdddddddddddhhhhhhy/'"
echo "hhhhddddddddddddddhhhhhhs-"
echo "hhdddddddddddddddddhhhhhhyo."
echo "dddddddddddddddddddddhhhhhhy/'                                                  "
echo ""
echo ""

#Set database version
/usr/bin/update_database.py

#Set default user
echo "populating konker database..."
#/usr/bin/populate_demo_data_roles.py
python /usr/bin/populate_users.py
echo "migrate roles..."
python /usr/bin/userskonker/migrate_user_roles.py

exec "$@"

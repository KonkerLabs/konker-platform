#
# make file to build artifact and docker image
# Maintainer: Andre Rocha <andre@konkerlabs.com>
# Version: 0.1
# Licence: To be defined
#
JCC = mvn
DCC = docker
REGISTRY_SRC = ../../
JFLAGS = -f $(REGISTRY_SRC)
JTARGET = clean install package -DskipTests
#ENV_SRC=env
#default target
default:
			$(JCC) $(JFLAGS) $(JTARGET)
			cp $(REGISTRY_SRC)/konker.registry/target/registry.war build/


install:
			$(DCC) build .


#
# make file to build artifact and docker image
# Maintainer: Andre Rocha <andre@konkerlabs.com>
# Version: 0.1
# Licence: To be defined
#
JCC = mvn
DCC = docker
REGISTRY_SRC = ../../konker.registry
JFLAGS = -f $(REGISTRY_SRC)
JTARGET = clean install package
ENV_SRC=env
#default target
default:
			$(RM) build/*
			$(JCC) $(JFLAGS) $(JTARGET)
			cp $(REGISTRY_SRC)/target/registry.war build/
			cp $(ENV_SRC)/$(ENV)/* build/


install:
			$(DCC) build .

clean:
				$(RM) build/*

# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help


APP_NAME=rhdg-on-eap
EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4

#########################
# Main Development tasks
#########################

run-eap:
ifndef EAP_HOME
	$(error EAP_HOME is undefined. Please, set the env var to the correct value. Example: `export EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4`)
endif
	sh ${EAP_HOME}/bin/standalone.sh -c standalone-ha.xml

deploy:
ifndef EAP_HOME
	$(error EAP_HOME is undefined. Please, set the env var to the correct value. Example: `export EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4`)
endif
	mvn clean package
	cp target/${APP_NAME}.war ${EAP_HOME}/standalone/deployments

undeploy:
ifndef EAP_HOME
	$(error EAP_HOME is undefined. Please, set the env var to the correct value. Example: `export EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4`)
endif
	rm -f ${EAP_HOME}/standalone/deployments/${APP_NAME}.war


create-cache-local:
ifndef EAP_HOME
	$(error EAP_HOME is undefined. Please, set the env var to the correct value. Example: `export EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4`)
endif
	${EAP_HOME}/bin/jboss-cli.sh -c --file=cache-config/standalone-local.cli

create-cache-clustered:
ifndef EAP_HOME
	$(error EAP_HOME is undefined. Please, set the env var to the correct value. Example: `export EAP_HOME=/home/alvaro/Downloads/RHDG/jboss-eap-7.4`)
endif
	${EAP_HOME}/bin/jboss-cli.sh -c --file=cache-config/standalone-cluster.cli

#########################
# Other development tasks
#########################


effective-pom:
	mvn clean help:effective-pom > effective-pom.txt

dependency-tree:
	mvn dependency:tree > dependency-tree.txt

verify:
	mvn clean verify -Dmaven.test.skip

install:
	clear
	mvn clean install -U -Dmaven.test.skip -Dcheckstyle.skip

package:
	clear
	mvn -f pom.xml clean install -Dcheckstyle.skip

test:
	mvn quarkus:test

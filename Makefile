PLUGIN=mcMMOIRC
CWD=$(shell pwd)
SRC=src
DEPS=.deps
OBJS=.objs

VERSION=$(shell grep version $(SRC)/plugin.yml | cut -d ' ' -f 2)
JAR=$(CWD)/$(PLUGIN)-$(VERSION).jar

SRCS=$(shell find $(SRC) -type f -name *.java -printf "%h/%f ")
JDEPS=$(shell find $(DEPS) -type f -printf "%h/%f:")

all: deps objs jar

jar:
	rm -f $(JAR)
	jar cvf $(JAR) -C $(OBJS) .

objs:
	rm -rf $(OBJS)
	mkdir -p $(OBJS)
	cp $(SRC)/plugin.yml $(OBJS)
	javac -cp $(JDEPS) -d $(OBJS) -g $(SRCS)

deps:
	rm -rf $(DEPS)
	mkdir -p $(DEPS)
	
	wget -O $(DEPS)/craftbukkit-1597.jar http://ci.bukkit.org/job/dev-CraftBukkit/1597/artifact/target/craftbukkit-1.0.1-R1.jar
	wget -O $(DEPS)/CraftIRC-55.jar      http://ensifera.com:8080/job/CraftIRC%203/55/com.ensifera$CraftIRC/artifact/com.ensifera/CraftIRC/3.0.0-SNAPSHOT/CraftIRC-3.0.0-SNAPSHOT.jar
	wget -O $(DEPS)/mcMMO-1.2.08-dev.jar http://dev.bukkit.org/media/files/566/904/mcMMO.jar

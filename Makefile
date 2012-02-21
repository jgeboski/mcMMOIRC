PLUGIN=mcMMOIRC
OUT=$(PLUGIN).jar

JC=javac
JFLAGS=-g

JAR=jar
MKDIR=mkdir
RM=rm
WGET=wget

CWD=$(shell pwd)

SRC=src
DEP=.dep

.SUFFIXES: .java .class

SRCS=\
  $(SRC)/org/mcmmoirc/Configuration.java \
  $(SRC)/org/mcmmoirc/Log.java \
  $(SRC)/org/mcmmoirc/Message.java \
  $(SRC)/org/mcmmoirc/MEndPoint.java \
  $(SRC)/org/mcmmoirc/EventListener.java \
  $(SRC)/org/mcmmoirc/mcMMOIRC.java \
  $(SRC)/org/mcmmoirc/command/CA.java \
  $(SRC)/org/mcmmoirc/command/CmcMMOIRC.java

DEPS=$(DEP)/bukkit.jar:$(DEP)/CraftIRC.jar:$(DEP)/mcMMO.jar:$(SRC)
OBJS=$(SRCS:.java=.class)

all: $(OUT)

$(OUT): objs
	$(JAR) cf $(OUT) -C $(SRC) plugin.yml $(OBJS)

objs: $(OBJS)

%.class: %.java
	$(JC) -classpath $(DEPS) -sourcepath $(SRC) $(JFLAGS) $<

deps:
	$(RM)    -rf $(DEP)
	$(MKDIR) -p  $(DEP)
	
	$(WGET) -O $(DEP)/bukkit.jar   http://ci.bukkit.org/job/dev-Bukkit/1211/artifact/target/bukkit-1.1-R3.jar
	$(WGET) -O $(DEP)/CraftIRC.jar http://dl.phozop.net/CraftIRC/v3/CraftIRC.jar
	$(WGET) -O $(DEP)/mcMMO.jar    http://dev.bukkit.org/media/files/574/155/mcMMO.jar

clean:
	$(RM) -f $(OBJS) $(OUT)


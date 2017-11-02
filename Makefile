empty :=
space := $(empty) $(empty)

LIBS = $(wildcard $(LIB_DIR)*)
CLASS_PATH = .$(subst $(space),:, $(LIBS))
OBJS_DIR = objs/
PACKAGE_DIR = com/HomeGrownProgramming/
BUILD_DIR = bin/
LIB_DIR = libs/
MANIFESTS_DIR = manifests/
CC = javac -Xlint:unchecked -d $(OBJS_DIR) -cp $(CLASS_PATH)
JAR = jar -cf 
RUNNABLE_JAR = jar -cfm

populate: $(PACKAGE_DIR)populate.java
	@mkdir -p $(OBJS_DIR)
	$(CC) $<
	@mkdir -p $(BUILD_DIR)
	$(RUNNABLE_JAR) $(patsubst $(PACKAGE_DIR)%.java,$(BUILD_DIR)%.jar, $<) $(MANIFESTS_DIR)$(patsubst $(PACKAGE_DIR)%.java,%.mf, $<) $(patsubst %.java, -C $(OBJS_DIR) %.class, $<) $(patsubst $(LIB_DIR)%, -C $(LIB_DIR) %, $(LIBS))

hw3: $(PACKAGE_DIR)hw3.java
	@mkdir -p $(OBJS_DIR)
	$(CC) $<
	@mkdir -p $(BUILD_DIR)
	$(RUNNABLE_JAR) $(patsubst $(PACKAGE_DIR)%.java,$(BUILD_DIR)%.jar, $<) $(MANIFESTS_DIR)$(patsubst $(PACKAGE_DIR)%.java,%.mf, $<) $(patsubst %.java, -C $(OBJS_DIR) %.class, $<) $(patsubst $(LIB_DIR)%, -C $(LIB_DIR) %, $(LIBS))

%.class : %.java
	@mkdir -p $(OBJS_DIR)
	$(CC) $<

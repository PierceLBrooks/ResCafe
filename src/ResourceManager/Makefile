# $Header$ 
#

# $Log$
#

#----------------------------------------------------------------------------
.SUFFIXES: .class .java


#JAVAC = javac 
JAVAC = jikes
VPATH = .:ResourceManager

#----------------------------------------------------------------------------
all: ResourceManager

ResourceManager : ResourceIntegers.class Resource.class ResourceType.class ResourceModel.class

.java.class:
	${JAVAC} -classpath .:..:${CLASSPATH}   $<

clean:  
	rm -f *.class

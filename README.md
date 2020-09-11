# Contextual Ego Network Library

## Introduction
The Contextual Ego Network (CEN) Library is responsible for the management of information stored in the local devices. The library manages the following type of information:
- The user's contexts.
- Alters in each context.
- The user's ego network and the interactions occuring in each context.
- Data structures that other modules (e.g. the graph mining module of D4.3 and the social media mining module of D4.8)

This information supports dynamic loading and unloading from memory while preserving pointers to data objects. Serialization is handled automatically. *All serialized classes require a (protected) default constructor.*

## Installation
[![](https://jitpack.io/v/helios-h2020/h.core-SocialEgoNetwork.svg)](https://jitpack.io/#helios-h2020/h.core-SocialEgoNetwork)

### Jar File Installation
This library can be downloaded as a [jar file](https://github.com/helios-h2020/h.core-SocialEgoNetwork/blob/master/jar/h.core-SocialEgoNetwork%201.0.3.jar), which can be added to a
Java project's dependencies.

### Gradle Installation
##### First step
Add the JitPack repository to your build file. In particular, add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

##### Second step
Add the dependency:

```
dependencies {
        implementation 'com.github.helios-h2020:h.core-SocialEgoNetwork:1.0.3'
}
```

### Maven Installation
##### First step
Add the JitPack repository to your build pom file:

```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

##### Second step
Add the dependency:

```xml
<dependency>
    <groupId>com.github.helios-h2020</groupId>
    <artifactId>h.core-SocialEgoNetwork</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API Usage
Adding an interaction to the contextual ego network by loading the respective objects or creating them when they don't already exist:
```java
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;

ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad("ego_user_id", null);
Context context = cen.getOrCreateContext("context_name");
Node alter1 = cen.getOrCreateNode("alter_id", null);
Interaction interaction = context.getOrAddEdge(cen.getEgo(), alter).addDetectedInteraction("interaction_type");
cen.save(); //saves the contextual ego network
```

Automating the saving process in a fault-tolerant manner:
```java
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.RecoveryListener;

ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad(getFilesDir().getPath(), "ego_user_id", null);
cen.addListener(new RecoveryListener()); //saves data in temporary log files that are resistant to device errors
```


Storing node-related parameters in the library (same usage for storing in contexts and edges):
```java
public class ModuleNodeParameters {
	private Integer[] parameters;
	public ModuleNodeParameters() {
		parameters = new Integer[10];
		//your parameter classes should implement only the default constructor (this enables the constructor-as-default-value) logic
		//class members can be initialized either in the constructor or when first needed
	}
}

Node node = ...;
ModuleNodeParameters nodeParameters = node.getOrCreateInstance(ModuleNodeParameters.class); //calls the default constructor
```

For more usage examples refer to the ExampleSave.java and ExampleLoad.java files of the eu.h2020.helios_social.core.contextualegonetwork package.

## Project Structure
This project contains the following components:

src - The source code files.

doc - Additional documentation files.

jar - Jar file installation.
# Contextual Ego Network Library

## Introduction
The Contextual Ego Network (CEN) Library is responsible for the management of information stored in the local devices. The library manages the following type of information:
- The user's contexts.
- Alters in each context.
- The user's ego network and the interactions occurring in each context.
- Data structures that other modules (e.g. the graph mining module of D4.3 and the social media mining module of D4.8) need to attach to the CEN

This information supports dynamic loading and unloading from memory while preserving pointers to data objects. Serialization is handled automatically. *All serialized classes require a (protected) default constructor.*

## Installation
[![](https://jitpack.io/v/helios-h2020/h.core-SocialEgoNetwork.svg)](https://jitpack.io/#helios-h2020/h.core-SocialEgoNetwork)

### Jar File Installation
This library can be downloaded as a [jar file](https://github.com/helios-h2020/h.core-SocialEgoNetwork/blob/master/jar/h.core-SocialEgoNetwork%201.1.1.jar), which can be added to a
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
        implementation 'com.github.helios-h2020:h.core-SocialEgoNetwork:1.1.1'
}
```

### Maven Installation
Firth, add the JitPack repository to your build pom file

```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

Then, add the dependency

```xml
<dependency>
    <groupId>com.github.helios-h2020</groupId>
    <artifactId>h.core-SocialEgoNetwork</artifactId>
    <version>1.1.1</version>
</dependency>
```

## API Usage

The main concepts of this API are also presented in the HELIOS platform's [tutorial page](https://helios-social.com/helios-for-devs/tutorials/) (look for "Core Component - Social Ego Network Manager")

### Creating or Loading the CEN for a Storage System
To create or load the contextual ego network, this module utilizes an abstraction of the
storage system's implementation through the abstract class `Storage`. If the aim is to
store saved files of the CEN module in Android, we recommend instantiating a `LegacyStorage`,
which is backwards compatible with earlier versions.

Storage instantiation is done through the `Storage.getInstance` method.
This takes two arguments: the file path at  which to instantiate the storage
and the storage class to instantiate. It must be noted
that the storage path may be completely cleared through some CEN operation and hence
no other data should reside inside. Hence, it is preferable to obtain an
app-specific Android storage path through `getFilesDir().getPath()`. 

The code below demonstrates how a storage can be created.


```java
import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.storage.NativeStorage;

String storagePath = getFilesDir().getPath(); // best practice for obtaining the android storage path
Storage storage = Storage.getInstance(storagePath, LegacyStorage.class);
```

Given a file system storage abstraction, like the one above, an instance of the 
contextual ego network can be loaded or created if it doesn't already exist through 
the method `ContextualEgoNetwor.getOrLoad`. The arguments to be passed to this method are
the abstracted storage system instance, as well as the node data required to create the
ego node if there is nothing to load - namely a String identifier of the ego node and a default
object to attach on the ego node.

The code below demonstrates how to create a `ContextualEgoNetwork` instance for an ego
node with local (i.e. CEN-specific) identifier `ego_user_id`. We remind that the ego node
corresponds to the user of the device. 

```java
import eu.h2020.helios_social.core.contextualegonetwork.Storage;

Storage storage = ...;
ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad(storage, "ego_user_id", null);
```


Adding an interaction to the contextual ego network by loading the respective objects or creating them when they don't already exist:

```java
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;

ContextualEgoNetwork cen = ...;
Context context = cen.getOrCreateContext("context_name");
Node alter1 = cen.getOrCreateNode("alter_id", null);
Interaction interaction = context.getOrAddEdge(cen.getEgo(), alter).addDetectedInteraction("interaction_type");
cen.save(); //saves the contextual ego network
```

Automating the saving process in a fault-tolerant manner:

```java
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.RecoveryListener;
import eu.h2020.helios_social.core.contextualegonetwork.Storage;

Storage storage = ...
ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad(storage, "ego_user_id", null);
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

For more usage examples refer to the ExampleSave.java and ExampleLoad.java files of the `eu.h2020.helios_social.core.contextualegonetwork.examples` package.

## Project Structure
This project contains the following components:

src - The source code files.

doc - Additional documentation files.

jar - Jar file installation.
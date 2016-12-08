irongpm
=====

This package contains the *experimental* graph pattern matching (GPM) engine.
It can be used to define and search for patterns within an [IF-MAP][1] Graph.
As a result, different actions can be performed, e.g. generating new information
or trigger other systems.
As of now, the system is used within the [SIMU project][2] environment, where actions
include incident reporting in the SIEM-GUI.


Configuration
=====

The project specific configuration consists of three main parts.

* The MAP-Server connection defines the address and credentials for the MAP-Server
* The [VisITMeta][3] dataservice connection describes the address of thr REST interface
* Rule-Management defines patterns and actions (handled in an own section later on)

The MAP-Server configuration is managed in the `irongpm.properties` file:
By default, the localhost and the credentials `gpm:gpm` are used.
The connection to the VistITMeta dataservice is configured in the 
`dataservice.yml` file. Again, `localhost` is the default option.

For the the database connection of the incident database (see the 
SIMU project description for further information) an addtional config file is maintained.
The connection and further database configuration parameters can be adapted in the
hibernate configuration (`hibernate.cfg.xml`) file.

Finally, logging can be configured in the `log4j.properties`

Rules
=====

The Rules are used to define the pattern in the graph and according actions.
Internally rules are handled as Java objects which consist of a pattern object (a typical IF-MAP Graph of Identifiers and Metadata) and according actions (as handlers) as well a textual description and recommendations for potential incident handling. 

The parts of the pattern are described by adding elements to the identifiers and metadata.
These properties can can be used in two ways (and combined):

1. Restricted properties: Where the value is not allowed to be present
2. "Related" properties: Where a placeholder is established.

    This placeholder is stored when it first is matched and can be either used for further comparison or in the actions and recommendations text when used within $-signs (e.g. `$placeholder$`)

Note: A property that is neither restricted nor related is just compared as expected in a comparison, so it is needed to be present and matching in value.

These patterns are compared against the MAP-graph and the actions are executed on match. Wildcards / Placeholders can be used to fill the action/recommendation with pattern specific data (e.g. add a server IP in the recommendation text).


Reflections:
-----
As of now, rules are loaded from external classes via reflections at project startup. Therefore the classes need to implement the `RuleLoader` interface, which defines one message to load a List of Rules.

An example RuleLoader is defined in irongpm-rules, which is located in the rule folder and can be used as a reference. The code bases is located in the sources at src/main/irongpm-rules.
Where possible, alternative approaches to hard-coding the patterns in the rule loader can be implemented (e.g. parse a rule- or graph description language to define the pattern - nevertheless the implemented interface stays the same!

Example:
-----
Exemplary Pattern/Rule and Action Types are provided by irongpm and can easily be used (just use irongpm as a dependency. E.g. by installing irongpm in your local repository (mvn install) and then referring to it in the pom.xml as in the irongpm-rules example!.

Define the pattern

    PatternGraphImpl ruleGraph = new PatternGraphImpl();
    PatternVertex dev = new BasicPatternVertex("device");
    PatternVertex ip = new BasicPatternVertex("ip-address");
	ip.addProperty("/ip-address[@type]", "IPv4", false, false); 
	//"type" attribute with value "IPv4" is required
	ip.addProperty("/ip-address[@value]", "ip", false, true); 
	//"value" attribute is also required, but the value is stored or compared with the placeholder "ip" (no specific value is compulsory)
    PatternMetadata devIpMeta = new BasicPatternMetadata("device-ip");
	devIpMeta.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue", false, false);
	ruleGraph.addVertex(dev);
	ruleGraph.addVertex(ip);
	PatternEdge devIpEdge = new BasicPatternEdge(dev, ip, devIpMeta);
	ruleGraph.addEdge(dev, ip, devIpEdge);
	ruleGraph.setPublishVertex(ip);
	
Define the rule:

	BasicPatternRule rule = new BasicPatternRule(ruleGraph, "Rule 1", "Example Description", "Example recommendation: Please check IP: $ip$");//The beforementioned placeholder "ip" ist used here -> syntax: $ip$
	rule.addAction(new IncidentAction());

Actions
----
As a result of a found (matched) pattern in the IF-MAP Graph the engine can 

* Publish `Event` or `UnexpectedBehavior` Metadata: Specify the insert point on pattern creation by 	`ruleGraph.setPublishVertex(PatternVertex pv);`
* Create SIEM-GUI incidents
* Log the rule match (using log4j)

The actions are used and added within the Rule-Classes (compare the RuleLoader example as shown above in the *Rules* section).


Building
========
This section describes, how to build irongpm.

Prerequisites
-------------
In order to build the project with Maven you need to install
[Maven 3][4] manually or via the package manager of your
operating system.

Currently, irongpm uses the external library called `simu-entities` by DECOIT GmbH, that is used to create incidents to the SIMU GUI from the SIMU research project. 
This Maven artifact is not available via Github or Maven central at the moment, but probably will be in the near future. Therefore it is currently delivered in the `libs` folder and used in the Maven pom respectively. The exact use to build the program may therefore change after an official release of the entity package.

To install the dependency manually, navigate to the deepest subfolder within `libs` and execute:

	$ mvn install:install-file -Dfile=./simu-entities-0.0.1-SNAPSHOT.jar -DgroupId=de.decoit -DartifactId=simu-entities -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar


Build irongpm
---------------
Now you can build irongpm by simply executing:

    $ mvn package

in the root directory of the irongpm project (the directory
containing this `README` file). Maven should download all further
needed dependencies for you.
After a successful build you should find a zip-archive called
`irongpm-<version>-bundle.zip` in the
`target` folder.

Running
=====
1. Start a MAP server, e.g. irond (Download section at [Trust@HsH website] [5] or via
[Github] [6]

2. Start the VisITMeta dataservice

2. Change your working directory to the root directory of the irongpm project. (E.g. the root folder of the unzipped bundle from the `Building` section.

3. Start irongpm via the provided start script:

	$ sh start.sh


Feedback
=====
If you have any questions, problems or comments, please contact
	f4-i-trust@lists.hs-hannover.de


LICENSE
=====
irongpm is licensed under the [Apache License, Version 2.0][7].

[1]: http://www.trustedcomputinggroup.org/resources/tnc_ifmap_binding_for_soap_specification
[2]: https://github.com/trustathsh/visitmeta
[3]: http://simu-project.de
[4]: https://maven.apache.org/download.htm
[5]: http://trust.f4.hs-hannover.de
[6]: https://github.com/trustathsh/irond.git
[7]: http://www.apache.org/licenses/LICENSE-2.0.html

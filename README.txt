NOTE! THIS PROJECT IS HIGHLY EXPERIMENTAL, AND IT IS NOT 
SUITABLE FOR PRODUCTION DEPLOYMENTS. USE AT YOUR OWN RISK.

The project consists of three parts:

* dpcm
* dpcm-api
* dpcm-wildfly

1) dpcm
   DPCM is a heartbeat mechanism for independent Java 
   processes that form a loosely coupled cluster. Cluster 
   coupling is done with JGroups (Java) library. It is a 
   mature toolkit for reliable broadcast communication, 
   and it can be configured in various ways.

   The hearbeat mechanism makes sure that each local process 
   is running properly: to do this dpcm uses two pieces of 
   information. First and most important thing is to check 
   whether the JVM process exists or not. Secondly, when 
   possible, a separate plugin can be used to make more 
   sophisticated queries: you can create your own plugin. 
   If a process dies it will be recreated by the hearbeat. 
   The creation of JVM processes can use configuration files 
   and arguments. At minimum, only multiple dpcm processes 
   are running with default configuration and inner processes. 

   The purpose of clustering is to guarantee that individual 
   processes meet the following condition: eventually there 
   can be only one domain controller (DC) in the cluster and 
   the other processes must be normal process controllers (PC). 
   Dpcm mainstains this condition by restarting proceses and 
   propagating process statuses within the cluster members 
   (i.e. group). To be able to execute restart operation 
   a group must have at least two running dpcm instances.

   Currently, DPCM is missing capability of handling cluster 
   partitions and merges. Also, we could enable scaling (i.e. 
   restarting new process instances) based on target: either 
   performance or robustness.

2) dpcm-api
   The API that the DPCM defines the service that the dpcm 
   uses to connect to the client process (if implemented).

3) dpcm-wildfly
   This is an example project showing how to use DPCM with 
   Wildfly Application Server (AS). Wildfly-client implements 
   the dpcm-api interface that is used by the DPCM. Ideally, 
   dpcm maintains one active Domain Coordinator (DC) in a 
   domain and makes sure that no manual intervention is 
   required when current DC crashes assuming there are enough 
   redundancy. Note that if a DC is down in a Wildfly domain 
   no update operations, like application deployments etc., 
   can be performed for domain. In summary, dpcm starts a Wildfly 
   process on a host with given configuration, and then checks 
   if it runs properly and that there is one DC in the cluster.
   If needed dpcm will restart processes to meet this condition. 
   Dpcm queries to Wildfly AS can be scripted with Groovy, but 
   only Java implementation is strictly required.

Run 'mvn clean install' to get started...

This software is licensed under GPLv3.

Copyright (C) 2014 Jari Kuusisto


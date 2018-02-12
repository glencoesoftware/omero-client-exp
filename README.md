OMERO client object experiment
==============================

OMERO client object experiment.  Used to explore the floor performance
of session joining semantics with an OMERO server with the goal of
achieving both maximum concurrency and throughput.  #100fps


Requirements
============

* OMERO 5.4.x+
* Java 8+


Development Installation
========================

1. Clone the repository::

        git clone git@github.com:glencoesoftware/omero-client-exp.git

1. Run the Gradle build and utilize the artifacts as required::

        ./gradlew installDist
        cd build/install
        ...


Eclipse Configuration
=====================

1. Run the Gradle Eclipse task::

        ./gradlew eclipse

1. Add a new Run Configuration with a main class of `com.glencoesoftware.omero.Main`:


Notes
=====

* `Ice.InitializationData` creation is *very* slow for some reason

* Secure connections are approximately 2x the overhead

* `omero.client` re-use is negligable due to the overhead mostly being
router, connection and session creation.  `Ice.Communicator` creation is
negligable.

* Savings of `uncheckedCast` are lost whenever a remote call actually has
to be made on a proxy.

* Router proxies, and by extension their connection, are bound to a session
and have to be recreated if the session changes.


References
==========

* https://forums.zeroc.com/discussion/1067/uncheckedcast-vs-checkedcast

* https://forums.zeroc.com/discussion/4355/multiple-connection-to-the-same-glacier-with-python

* https://forums.zeroc.com/discussion/3908/multiple-connection-to-the-same-glacier


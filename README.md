# Terrer-WAPO

A Terrier Collection implementation for the WAPO collection

## Installation

Terrier can pick up the code from a local Maven repository. You should install into the local Maven repo.

	git clone <this repo>
	cd terrier-wapo
	mvn install
	

## Configuration and Usage in Terrier

You need to adjust the terrier.properties file in order to use this plugin to index the WAPO corpus 

	#import the terrier-wapo plugin
	terrier.mvn.coords=uk.ac.gla.dcs.terrierteam:terrier-wapo:0.1
	
	#tell indexing to use it
	trec.collection.class=uk.ac.gla.terrier.indexing.WAPOCollection
	
	#use more characters to record the doclength
	indexer.meta.forward.keylens=40
	

Then the following standard Terrier commands should be used to create the index:

	bin/trec_setup.sh <path to the WAPO corpus>
	bin/terrier batchindexing

## Credits

 - Craig Macdonald, University of Glasgow

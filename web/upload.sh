#!/usr/bin/env bash
scp -i $HOME/.ssh/lappsgrid-shared-key.pem target/solr-indexer.jar ubuntu@149.165.169.127:/home/ubuntu

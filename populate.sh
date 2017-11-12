#!/bin/bash
java -Xmx6G -jar populate.jar -d -host 192.168.1.151 -port 5002 -dbname mdb YelpDataset/yelp_business.json YelpDataset/yelp_review.json YelpDataset/yelp_user.json

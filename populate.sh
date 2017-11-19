#!/bin/bash
java -Xmx25G -jar populate.jar -d -host www.ryanloringcooper.info -port 5002 -dbname mdb YelpDataset/yelp_business.json YelpDataset/yelp_review.json YelpDataset/yelp_user.json

INDEX_NAME=test
curl -XDELETE "localhost:9200/$INDEX_NAME?pretty"
curl -XPUT "localhost:9200/$INDEX_NAME?pretty"
sleep 3
curl -XPUT "localhost:9200/$INDEX_NAME/_mapping/channelMetadataResponse?pretty" -d mapping.json
sleep 3
curl -XPUT "localhost:9200/$INDEX_NAME/chanelMetadataResponse/1?pretty" --data @/var/folders/k4/s86x689130l048d_764xsmzm0000gn/T/kaksidi4753688949408615528json
curl -XPUT "localhost:9200/$INDEX_NAME/chanelMetadataResponse/1?pretty" --data @/var/folders/k4/s86x689130l048d_764xsmzm0000gn/T/kaksidi1126227051485390327json
sleep 3
curl -XGET "localhost:9200/$INDEX_NAME/_search?pretty\&q=channelMetadataResponse.metaData.currentEvent.artists.name:Abott%20;Costello"

FROM elasticsearch:2.3.3

ENV PATH=/usr/share/elasticsearch/bin:$PATH \
    ES_DIR="/usr/share/elasticsearch" \
    ES_HOME="/etc/elasticsearch" \
    ES_URL="http://127.0.0.1:9200" \
    ES_CLUSTER_NAME="shield-test" \
    ES_CLUSTER_HOST="127.0.0.1"


# set working dir for build
WORKDIR ${ES_DIR}

# The shield plugin configuration must be in ES_HOME/config
#COPY config/elasticsearch_shield.yml > ${ES_HOME}/elasticsearch.yml
RUN rm -rf config && ln -s /etc/elasticsearch/ config

#install plugins
RUN bin/plugin install --batch license \
      && bin/plugin install --batch shield

# copy configs for vanilla ES
#COPY config/elasticsearch.yml config/

# copy configs file for SHIELDed ES
COPY config/elasticsearch.yml ${ES_HOME}/elasticsearch.yml
COPY config/shield/ ${ES_HOME}/shield/

EXPOSE 9200 9300 


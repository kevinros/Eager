package org.lappsgrid.eager.core

/**
 *
 */
class Configuration {

    public String HOST
    public String BROADCAST
    public String POSTOFFICE
    public String BOX_ERROR
    public String BOX_LOAD
    public String BOX_NLP_STANFORD

    Configuration() {
        HOST = "rabbitmq.lappsgrid.org"
        BROADCAST = "eager.broadcast"
        POSTOFFICE = "eager.postoffice"
        BOX_ERROR = "error"
        BOX_LOAD = "load"
        BOX_NLP_STANFORD = "nlp.stanford"
    }

    Configuration(File file) {
        this(file.text)
    }

    Configuration(URL url) {
        this(url.text)

    }

    Configuration(String script) {
        ConfigObject config = new ConfigSlurper().parse(script)
        HOST = config.get('host', '129.114.17.244')
        BROADCAST = config.get('broadcast', 'eager.broadcast')
        POSTOFFICE = config.get('postoffice', 'eager.postoffice')
        BOX_ERROR = config.pobox?.error ?: 'error'
        BOX_LOAD = config.pobox?.load ?: 'load'
        BOX_NLP_STANFORD = config.pobox?.nlp?.stanford ?: 'nlp.stanford'
    }

}

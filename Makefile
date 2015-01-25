.PHONY: _pwd_prompt decrypt_conf encrypt_conf

CONF_FILE=ant.properties

# _priv task to echo instr
_pwd_prompt:
	@echo "contact:evandrix@gmail.com"

# to create ${CONF_FILE}
decrypt_conf: _pwd_prompt
	openssl cast5-cbc -d -in ${CONF_FILE}.cast5 -out ${CONF_FILE}
	chmod 0600 ${CONF_FILE}

# for updating ${CONF_FILE}
encrypt_conf: _pwd_prompt
	openssl cast5-cbc -e -in ${CONF_FILE} -out ${CONF_FILE}.cast5

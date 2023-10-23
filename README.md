# vk-crawler
Программа для выкачивания постов из контакта и поиска по ним

Инструкция по запуску
1) скачать солр 9.4.0
2) распаковать его
3) зайти в solr-9.4.0/bin и запустить солр командой
   ./solr start
4) зайти в браузере на http://localhost:8983/solr
5) создать ядро records с конфигами из папки solr_config
6) написать свой access_token в src/main/resources/application.properties
7) выполнить mvn clean package
8) зайти в папку target
   cd target
9) запустить джарник vk-crawler.jar
   java -jar vk-crawler.jar
10) зайти в браузере на http://localhost:8080/

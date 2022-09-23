# CamelExampleForDemonstration

Сервис состоит из нескольких потоков:
 - читает файлы по с учетом фильтра и в зависимости от расширения по-разному отправляет их на почту.
 - принимает SOAP запроса и передаёт его содержимое в очередь RabbitMQ
 - принимает из очереди RabbitMQ и отправляет в soap endpoint

Порядок работы с сервисом:
- В файле application.properties указать параметры доступа к RabbitMQ,
а также очереди и Exchange, куда будут помещаться сообщения и откуда будут приходить.
Проверить порт, на котором будет запускаться сервис.

- Создать папки для чтения файлов.
По умолчанию:
 для чтения: c:/1/
 для перемещения "отработанных": C:/1/History/
Настраивается в CamelRoute.java.

- Добавить описание soap endpoint, в которую будем отправлять из очереди RabbitMQ.

- В файле email.properties изменить настройки для доступа к почтовому ящику.
Перед этим необходимо сделать настройки в личном кабинете своего почтового ящика (при необходимости).

- Запустить сервис.

- Скопировать файлы в директорию для чтения. В логах в течение 10с убедиться, что файл прочитан,
после чего будет перенесен в папку History.

- Убедиться, что открывается WSDL по адресу вида:
http://localhost:8080/cxf/Sender?wsdl

- В SoapUI создать новый SOAP проект. В поле Initial WSDL указать адрес WSDL

- Создать запрос в методе SendRequest. Сообщений может быть сколько угодно.
delay - задержка меред отправкой сообщения
text - текст сообщения

Например:
```xml
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sat="http://www.satel.org/">
       <soapenv:Header/>
       <soapenv:Body>
          <sat:data>
             <!--1 or more repetitions:-->
             <mess>
                <delay>100</delay>
                <text>TestMessageFirst</text>
             </mess>
             <mess>
                <delay>200</delay>
                <text>TestMessageSecond</text>
             </mess>
          </sat:data>
       </soapenv:Body>
    </soapenv:Envelope>
```    

- Отправить сообщение и смотреть в логи сервиса и в очередь RabbitMQ.
Если всё завершилось успешно, в SoapUI будет получено ответное сообщение:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Body>
      <ns2:result xmlns:ns2="http://www.satel.org/">
         <message>All OK!</message>
      </ns2:result>
   </soap:Body>
</soap:Envelope>
```

- Во входящую очеред RabbitMQ отправить запрос, вида:
```xml
      <sat:data>
         <!--1 or more repetitions:-->
         <mess>
            <delay>100</delay>
            <text>TestMessageFirst</text>
         </mess>
         <mess>
            <delay>200</delay>
            <text>TestMessageSecond</text>
         </mess>
      </sat:data>
```
- В логах убедиться, что он пришел корректно, а так был отправлен в исходящий soap сервис.

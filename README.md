# ZosShell

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demo.gif)
  
This project demonstrates the usage of [Zowe Java SDK](https://github.com/zowe/zowe-client-java-sdk).
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs the following linux commands:  
  
    cat                      - display contents
    cd <arg>                 - where arg is a dataset value or empty
    get <arg>                - where arg is a job name - output job log
    history <arg>            - where arg is optional and identicates the number to display from bottom   
    !n                       - where n is a number, to execute command nummber n in history list   
    !string                  - will execute the last history command starting with that “string”
    ls <arg>                 - where arg is a dataset value or empty 
    ls -l <arg>              - where arg is a dataset value or empty 
    ps                       - display all processes running
    ps <arg>                 - where arg is a task/job name   
    pwd                      - show current working dataset
    rm <arg>                 - where arg is "*", member, dataset, or dataset with member value
    tail <arg1> <arg2>       - where arg1 is job name, arg2 is optional and is line limit - tail job log
    touch <arg>              - create member arg if it does not already exist
    uname                    - show current connected host name
    whoami                   - show current connected user name
  
Along with following custom commands:  
    
    count members           - return member count in dataset
    count datasets          - return dataset count in dataset
    cp | copy <arg> arg>    - where arg can be ".", member, dataset or dataset(member)  
    cancel <arg>            - where arg is a task/job name  
    connections             - a list of connection(s)   
    change <arg>            - where arg is a number representing a connection
    download <arg>          - download member/sequential dataset to your local machine under c:\ZosShell location  
    end                     - end session closes shell UI window
    submit <arg>            - where arg is a member name  
    v | visited             - a list of visited datasets  
  
The following key combinations provide the following functionality within the shell:  
  
    CTRL C                  - copy text
    CTRL V                  - paste coped text
    UP arrow                - scroll up through history list
    DOWN arrow              - scroll down through history list
    
To quit from the command shell UI, you can either press 'X' windows close icon or enter 'end' keyword.  
  
## Requirements  
  
    Java 11 to execute and build the target jar file.   
    z/OSMF installed on your backend mainframe instance.  
    
## Build And Execute  

Create a credentials.txt under the C:\ZosShell drive directory that contains a list of z/OSMF connections per line with a comma delimiter for
connection values. You can specify drive location by changing the hard coded value in the code.  
  
Format:  
    
    hostname,zomsfportnumber,username,password  
    
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar  
  


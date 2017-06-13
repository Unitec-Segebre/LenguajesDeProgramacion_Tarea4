FROM flangelier/scala


RUN wget https://github.com/Unitec-Segebre/LenguajesDeProgramacion_Tarea4/archive/master.zip

RUN unzip master.zip


EXPOSE 8080


CMD cd LenguajesDeProgramacion_Tarea4-master && scala -classpath .:gson-2.6.2.jar tarea4.jar
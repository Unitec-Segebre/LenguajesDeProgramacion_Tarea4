FROM williamyeh/scala

RUN git clone https://github.com/Unitec-Segebre/LenguajesDeProgramacion_Tarea4.git

EXPOSE 8080

CMD cd LenguajesDeProgramacion_Tarea4 && scala -cp .:gson-2.6.2.jar:tarea4.scala
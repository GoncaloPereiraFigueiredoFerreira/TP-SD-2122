# TP-SD-2122

Projeto de gestão de voos realizado no ambito da UC de Sistemas Distribuidos, no curso Licenciatura em Engenharia Informática em Universidade do Minho.

Final Grade: 19 / 20

Foram desenvolvidos 3 executáveis:

  - MainCliente: 
      Permite executar operações de cliente/administrador. É necessário inicio prévio do servidor.
  ```
    java -jar client.jar
  ```  

  - ServerMain: 
      Inicia o servidor.
  ```
    java -jar server.jar
  ```  

  - Simulador: 
      Inicia um servidor e X clientes. Cada cliente cria uma conta, autentica-se, 
      efetua Y (fornecido pelo utilizador) tentativas, sequenciais, de reserva de viagens aleatórias (com ou sem escalas), 
      remove todas as reservas efetuadas, e finalmente efetua um pedido de listagem das reservas, 
      de forma a comprovar que foram todas removidas com sucesso.
      Se o número de clientes - valor X introduzido pelo utilizador - for suficientemente grande, 
      é possível iniciar instâncias individuais de MainCliente, permitindo uma aproximação ao uso "real".
  ```
    java -jar simulador.jar
  ```  



Realizado por:  
  Alexandre Martins  
  Gonçalo Ferreira  
  Gonçalo Santos   
  Luis Silva   

#!/bin/bash
#!/usr/bin/perl

cd Entrada1

for arq in *.txt
do

	arqSemExt="${arq%.*}"	#Nome do arquivo sem extensão

	#sed 's;{S};;g; s;\\.;.;g' $arq > $arqSemExt"1.txt" #Estava dando problemas ao processar sigarra
	sed 's;{S};;g;' $arq > $arqSemExt"1.txt"

	#Como marca tempo dentro de DOC no caso da Tribuna, faço essa substituição aqui...
	sed -i '/^<DOC DOCID=/d; /<\/DOC>/d;' $arqSemExt"1.txt"

	# Reconhece e anota expressões com categoria (qq categoria). Se quiser só categoria ou tipo e subtipo, tenho que acrescentar novas linhas semelhantes a essa para reconhecê-las e anotá-las
	#perl -pe 's/<(.*?)>(.*?)<\/(.*?)>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"$1\">$2<\/EM>"/eg' $arqSemExt"1.txt" > $arqSemExt"_anotado.txt"

	#PARA ANOTAR SÓ PESSOA(INDIVIDUAL) e ignora as outras anotações
	#perl -pe 's/<PESSOA>(.*?)<\/PESSOA>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\" TIPO=\"INDIVIDUAL\">$1<\/EM>"/eg' $arqSemExt"1.txt" > $arqSemExt"_anotado1.txt"
	#perl -pe 's/<PESSOA>(.*?)<\/PESSOA>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>"/eg' $arqSemExt"1.txt" > $arqSemExt"_anotado1.txt"
	#perl -pe 's/<LOCAL>(.*?)<\/LOCAL>/$1/eg' $arqSemExt"_anotado1.txt" > $arqSemExt"_anotado2.txt"
	#perl -pe 's/<ORGANIZACAO>(.*?)<\/ORGANIZACAO>/$1/eg' $arqSemExt"_anotado2.txt" > $arqSemExt"_anotado3.txt"
	#perl -pe 's/<TEMPO>(.*?)<\/TEMPO>/$1/eg' $arqSemExt"_anotado3.txt" > $arqSemExt"_anotado4.txt"
	#perl -pe 's/<VALOR>(.*?)<\/VALOR>/$1/eg' $arqSemExt"_anotado4.txt" > $arqSemExt"_anotado5.txt"
	#perl -pe 's/<ABSTRACCAO>(.*?)<\/ABSTRACCAO>/$1/eg' $arqSemExt"_anotado5.txt" > $arqSemExt"_anotado6.txt"
	#perl -pe 's/<ACONTECIMENTO>(.*?)<\/ACONTECIMENTO>/$1/eg' $arqSemExt"_anotado6.txt" > $arqSemExt"_anotado7.txt"
	#perl -pe 's/<OBRA>(.*?)<\/OBRA>/$1/eg' $arqSemExt"_anotado7.txt" > $arqSemExt"_anotado8.txt"
	#perl -pe 's/<COISA>(.*?)<\/COISA>/$1/eg' $arqSemExt"_anotado8.txt" > $arqSemExt"_anotado9.txt"
	#perl -pe 's/<OUTRO>(.*?)<\/OUTRO>/$1/eg' $arqSemExt"_anotado9.txt" > $arqSemExt"_anotado.txt"
	
	#PARA ANOTAR TODAS AS ENTIDADES

	perl -pe 'my $numId=10000; s/<PESSOA>(.*?)<\/PESSOA>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>"/eg' $arqSemExt"1.txt" > $arqSemExt"_anotado1.txt"

    perl -pe 'my $numId=14000; s/<PESSOA-RELACAO_FAMILIAR>(.*?)<\/PESSOA-RELACAO_FAMILIAR>(.*?)<PESSOA-RELACAO_FAMILIAR>(.*?)<\/PESSOA-RELACAO_FAMILIAR>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>$2<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\" TIPOREL=\"relacao_familiar\" COREL=\"'$arqSemExt'-".($numId - 2)."\">$3<\/EM>"/eg' $arqSemExt"_anotado1.txt" > $arqSemExt"_anotado2.txt"

    perl -pe 'my $numId=18000; s/<PESSOA-IDENT>(.*?)<\/PESSOA-IDENT>(.*?)<PESSOA-IDENT>(.*?)<\/PESSOA-IDENT>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>$2<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\" TIPOREL=\"ident\" COREL=\"'$arqSemExt'-".($numId - 2)."\">$3<\/EM>"/eg' $arqSemExt"_anotado2.txt" > $arqSemExt"_anotado3.txt"
    
    perl -pe 'my $numId=19000; s/<PESSOA-LOCAL_NASCIMENTO>(.*?)<\/PESSOA-LOCAL_NASCIMENTO>(.*?)<LOCAL-LOCAL_NASCIMENTO>(.*?)<\/LOCAL-LOCAL_NASCIMENTO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>$2<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCAL\" TIPOREL=\"local_nascimento_de\" COREL=\"'$arqSemExt'-".($numId - 2)."\">$3<\/EM>"/eg' $arqSemExt"_anotado3.txt" > $arqSemExt"_anotado4.txt"

	perl -pe 'my $numId=20000; s/<LOCAL>(.*?)<\/LOCAL>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCAL\">$1<\/EM>"/eg' $arqSemExt"_anotado4.txt" > $arqSemExt"_anotado5.txt"

    perl -pe 'my $numId=28000; s/<LOCAL-OCORRE_EM>(.*?)<\/LOCAL-OCORRE_EM>(.*?)<ORGANIZACAO-OCORRE_EM>(.*?)<\/ORGANIZACAO-OCORRE_EM>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCAL\">$1<\/EM>$2<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ORGANIZACAO\" TIPOREL=\"ocorre_em\" COREL=\"'$arqSemExt'-".($numId - 2)."\">$3<\/EM>"/eg' $arqSemExt"_anotado5.txt" > $arqSemExt"_anotado6.txt"

    perl -pe 'my $numId=29000; s/<LOCAL-INCLUI>(.*?)<\/LOCAL-INCLUI>(.*?)<LOCAL-INCLUI>(.*?)<\/LOCAL-INCLUI>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCAL\">$1<\/EM>$2<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCAL\" TIPOREL=\"inclui\" COREL=\"'$arqSemExt'-".($numId - 2)."\" FACS_ORIGEM=\"LOCAL\" FACS_ALVO=\"LOCAL\">$3<\/EM>"/eg' $arqSemExt"_anotado6.txt" > $arqSemExt"_anotado7.txt"

	perl -pe 'my $numId=30000; s/<ORGANIZACAO>(.*?)<\/ORGANIZACAO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ORGANIZACAO\">$1<\/EM>"/eg' $arqSemExt"_anotado7.txt" > $arqSemExt"_anotado8.txt"

	perl -pe 'my $numId=40000; s/<TEMPO>(.*?)<\/TEMPO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"TEMPO\">$1<\/EM>"/eg' $arqSemExt"_anotado8.txt" > $arqSemExt"_anotado9.txt"

	perl -pe 'my $numId=50000; s/<VALOR>(.*?)<\/VALOR>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"VALOR\">$1<\/EM>"/eg' $arqSemExt"_anotado9.txt" > $arqSemExt"_anotado10.txt"

	perl -pe 'my $numId=60000; s/<ABSTRACCAO>(.*?)<\/ABSTRACCAO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ABSTRACCAO\">$1<\/EM>"/eg' $arqSemExt"_anotado10.txt" > $arqSemExt"_anotado11.txt"

	perl -pe 'my $numId=70000; s/<ACONTECIMENTO>(.*?)<\/ACONTECIMENTO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ACONTECIMENTO\">$1<\/EM>"/eg' $arqSemExt"_anotado11.txt" > $arqSemExt"_anotado12.txt"

	perl -pe 'my $numId=80000; s/<OBRA>(.*?)<\/OBRA>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"OBRA\">$1<\/EM>"/eg' $arqSemExt"_anotado12.txt" > $arqSemExt"_anotado13.txt"

	perl -pe 'my $numId=90000; s/<COISA>(.*?)<\/COISA>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"COISA\">$1<\/EM>"/eg' $arqSemExt"_anotado13.txt" > $arqSemExt"_anotado14.txt"

	perl -pe 'my $numId=100000; s/<OUTRO>(.*?)<\/OUTRO>/" <EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"OUTRO\">$1<\/EM>"/eg' $arqSemExt"_anotado14.txt" > $arqSemExt"_anotado.txt"

	#PARA ANOTAR AS ENTIDADES DO SIGARRA
	#perl -pe 's/<Pessoa>(.*?)<\/Pessoa>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"PESSOA\">$1<\/EM>"/eg' $arqSemExt"1.txt" > $arqSemExt"_anotado1.txt"
	#perl -pe 's/<Localizacao>(.*?)<\/Localizacao>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"LOCALIZACAO\">$1<\/EM>"/eg' $arqSemExt"_anotado1.txt" > $arqSemExt"_anotado2.txt"
	#perl -pe 's/<Organizacao>(.*?)<\/Organizacao>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ORGANIZACAO\">$1<\/EM>"/eg' $arqSemExt"_anotado2.txt" > $arqSemExt"_anotado3.txt"
	#perl -pe 's/<Data>(.*?)<\/Data>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"DATA\">$1<\/EM>"/eg' $arqSemExt"_anotado3.txt" > $arqSemExt"_anotado4.txt"
	#perl -pe 's/<Hora>(.*?)<\/Hora>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"HORA\">$1<\/EM>"/eg' $arqSemExt"_anotado4.txt" > $arqSemExt"_anotado5.txt"
	#perl -pe 's/<Evento>(.*?)<\/Evento>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"EVENTO\">$1<\/EM>"/eg' $arqSemExt"_anotado5.txt" > $arqSemExt"_anotado6.txt"
	#perl -pe 's/<Unidadeorganica>(.*?)<\/Unidadeorganica>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"UNIDADEORGANICA\">$1<\/EM>"/eg' $arqSemExt"_anotado6.txt" > $arqSemExt"_anotado7.txt"
	#perl -pe 's/<Curso>(.*?)<\/Curso>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"CURSO\">$1<\/EM>"/eg' $arqSemExt"_anotado7.txt" > $arqSemExt"_anotado.txt"
	#perl -pe 's/<ABSTRACCAO>(.*?)<\/ABSTRACCAO>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"ABSTRACCAO\">$1<\/EM>"/eg' $arqSemExt"_anotado5.txt" > $arqSemExt"_anotado6.txt"
	#perl -pe 's/<OBRA>(.*?)<\/OBRA>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"OBRA\">$1<\/EM>"/eg' $arqSemExt"_anotado7.txt" > $arqSemExt"_anotado8.txt"
	#perl -pe 's/<COISA>(.*?)<\/COISA>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"COISA\">$1<\/EM>"/eg' $arqSemExt"_anotado8.txt" > $arqSemExt"_anotado9.txt"
	#perl -pe 's/<OUTRO>(.*?)<\/OUTRO>/"<EM ID=\"'$arqSemExt'-".($numId++)."\" CATEG=\"OUTRO\">$1<\/EM>"/eg' $arqSemExt"_anotado9.txt" > $arqSemExt"_anotado.txt"

	#PARA ERROS NA CASCATA DE ENTIDADES PERDIDAS
	sed -i "s/<PESSOA>//g; s/<\/PESSOA>//g; s/<LOCAL>//g; s/<\/LOCAL>//g; s/<ORGANIZACAO>//g; s/<\/ORGANIZACAO>//g; s/<TEMPO>//g; s/<\/TEMPO>//g; s/<VALOR>//g; s/<\/VALOR>//g; s/<ABSTRACCAO>//g; s/<\/ABSTRACCAO>//g; s/<ACONTECIMENTO>//g; s/<\/ACONTECIMENTO>//g; s/<OBRA>//g; s/<\/OBRA>//g; s/<COISA>//g; s/<\/COISA>//g; s/<OUTRO>//g; s/<\/OUTRO>//g;" $arqSemExt"_anotado.txt"

	mv $arqSemExt"_anotado.txt" ../Entrada/$arqSemExt".txt" 
	rm $arqSemExt"1.txt" $arqSemExt"_anotado"*.txt

done

cd ..

	

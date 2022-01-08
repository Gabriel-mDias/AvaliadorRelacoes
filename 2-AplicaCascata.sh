#!/bin/bash


#Endereços
toolLogger="/home/gabriel/Unitex-GramLab-3.1/App/UnitexToolLogger"
#toolLogger="/home/juliana/Unitex/App/UnitexToolLogger"
alfabeto="/home/gabriel/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Alphabet.txt"
#alfabeto="/home/juliana/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Alphabet.txt"
normalizar="/home/gabriel/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Norm.txt"
#normalizar="/home/juliana/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Norm.txt"
concordAux="/home/gabriel/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Alphabet_sort.txt"
#concordAux="/home/juliana/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Alphabet_sort.txt"
dic="/home/gabriel/Unitex-GramLab-3.1/Portuguese (Brazil)/Dela"
#dic="/home/juliana/Unitex/Portuguese (Brazil)/Dela"

grafos="/home/gabriel/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Graphs/"
#grafos="/home/juliana/workspace/Unitex-GramLab/Unitex/Portuguese (Brazil)/Graphs"

	#Nome da cascata, recebido pelo 1-AnotaColecao-ComLG.sh, sem extensão
cascata="../Grafos/$1"

	#Classificações para cascata - Considera as 10 clasificações do Harem (Nada impede de adicionar mais)
ENTIDADES_NOMEADAS=('LOCAL' 'PESSOA' 'ORGANIZACAO' 'ABSTRACCAO' 'ACONTECIMENTO' 'COISA' 'OBRA' 'TEMPO' 'VALOR' 'OUTRO', 'LOCAL-LOCAL_NASCIMENTO' 'PESSOA-RELACAO_FAMILIAR' 'PESSOA-LOCAL_NASCIMENTO' 'PESSOA-IDENT' 'LOCAL-OCORRE_EM' 'ORGANIZACAO-OCORRE_EM' 'LOCAL-INCLUI' )


cd Entrada1

for i in *txt
do
	#Pega nome do arquivo sem extensão .txt
	arq="${i%.*}"

	mkdir $arq"_snt"
	dirAtual=$(pwd); #obtendo o endereço atual

	$toolLogger Normalize "$arq.txt" "-r$normalizar" "--output_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	$toolLogger Grf2Fst2 "$grafos/Preprocessing/Sentence/Sentence.grf" -y "--alphabet=$alfabeto" -qutf8-no-bom

	$toolLogger Flatten "$grafos/Preprocessing/Sentence/Sentence.fst2" --rtn -d5 -qutf8-no-bom

	$toolLogger Fst2Txt "-t$arq.snt" "$grafos/Preprocessing/Sentence/Sentence.fst2" "-a$alfabeto" -M "--input_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" "--output_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	$toolLogger Grf2Fst2 "$grafos/Preprocessing/Replace/Replace.grf" -y "--alphabet=$alfabeto" -qutf8-no-bom

	$toolLogger Fst2Txt "-t$arq.snt" "$grafos/Preprocessing/Replace/Replace.fst2" "-a$alfabeto" -R "--input_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" "--output_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	$toolLogger Tokenize "$arq.snt" "-a$alfabeto" "--input_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" "--output_offsets=$dirAtual/"$arq"_snt/tokenize.out.offsets" -qutf8-no-bom

	$toolLogger Dico "-t$arq.snt" "-a$alfabeto" "-m$dic/DELACF_PB.bin" "-m$dic/DELAF_PB.bin" "-m$dic/DELAF_PB_2015.bin" "-m$dic/dela-en-public.bin" "$dic/DELAF_PB_2015.bin" "$dic/DELACF_PB.bin" -qutf8-no-bom

	$toolLogger SortTxt "$dirAtual/"$arq"_snt/dlf" "-l$dirAtual/"$arq"_snt/dlf.n" "-o$concordAux" -qutf8-no-bom

	$toolLogger SortTxt "$dirAtual/"$arq"_snt/dlc" "-l$dirAtual/"$arq"_snt/dlc.n" "-o$concordAux" -qutf8-no-bom

	$toolLogger SortTxt "$dirAtual/"$arq"_snt/err" "-l$dirAtual/"$arq"_snt/err.n" "-o$concordAux" -qutf8-no-bom

	$toolLogger SortTxt "$dirAtual/"$arq"_snt/tags_err" "-l$dirAtual/"$arq"_snt/tags_err.n" "-o$concordAux" -qutf8-no-bom

	$toolLogger Cassys "-a$alfabeto" "-t$arq.snt" "-l$cascata" "-w$dic/DELACF_PB.bin" "-w$dic/DELAF_PB.bin" "-w$dic/DELAF_PB_2015.bin" "-w$dic/dela-en-public.bin" -v "-r$grafos/" "--input_offsets=$dirAtual/"$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	$toolLogger Concord "$dirAtual/"$arq"_snt/concord.ind" "-fCourier new" -s12 -l40 -r55 --html "-a$concordAux" --CL -qutf8-no-bom

	#Alterando o arquivo para o padrão, com tags html
	cat $arq"_csc.txt" > $arq"_temporario1.txt"


	#Primeiro passo, mudar todas as entidades relevantes para o formato html
	for ((cont=0; cont<${#ENTIDADES_NOMEADAS[@]}; cont++)); do
		sed -i 's/<code>'${ENTIDADES_NOMEADAS[$cont]}'<\/code>/<\/'${ENTIDADES_NOMEADAS[$cont]}'>/g' $arq"_temporario1.txt"

	done

	sed -i 's/<code>[^<]*<\/code>//g' $arq"_temporario1.txt"

		
	#Remover tags não relevantes
	sed 's/<form>//g; s/<\/form>//g' $arq"_temporario1.txt" > $arq"_temporario2.txt"

	#Remover tags não inclusas no vetor, até a mais interna, por isso é necessário um loop até que não ocorra mais nenhuma mudança
	sed 's/<csc>\([^<]*\)<\/csc>/\1/g' $arq"_temporario2.txt" > $arq"_temporario3.txt"

	comparar=$(cmp $arq"_temporario2.txt" $arq"_temporario3.txt")

	cat $arq"_temporario3.txt" > $arq"_temporario4.txt"

	while [[ "$comparar" != "" ]];
	do
		cat $arq"_temporario4.txt" > $arq"_temporario3.txt"
		sed 's/<csc>\([^<]*\)<\/csc>/\1/g' $arq"_temporario3.txt" > $arq"_temporario4.txt"
		comparar=$(cmp $arq"_temporario3.txt" $arq"_temporario4.txt")
	done


	#quarto passo: Trocar a primeira <csc> por <LOCAL>
	#<LOCAL> Rua do Hospício</LOCAL></csc>
	for ((cont=0; cont<${#ENTIDADES_NOMEADAS[@]}; cont++)); 
	do
		sed -i 's/<csc>\([^<]*\)<\/'${ENTIDADES_NOMEADAS[$cont]}'><\/csc>/<'${ENTIDADES_NOMEADAS[$cont]}'>\1<\/'${ENTIDADES_NOMEADAS[$cont]}'>/g' $arq"_temporario4.txt"
	done


	#Remover tags mais interna, por isso é necessário um loop até que não ocorra mais nenhuma mudança
	sed 's/<csc>\([^<]*\)<[[:upper:]]*>\([^<]*\)<\/[[:upper:]]*>\([^<]*\)<\/\([^<]*\)><\/csc>/<\4>\1 \2 \3<\/\4>/g' $arq"_temporario4.txt" > $arq"_temporario5.txt"
	

	comparar=$(cmp $arq"_temporario4.txt" $arq"_temporario5.txt")
	cat $arq"_temporario5.txt" > $arq".txt"
	

	while [[ "$comparar" != "" ]];
	do
		cat $arq".txt" > $arq"_temporario5.txt"

		sed 's/<csc>\([^<]*\)<[[:upper:]]*>\([^<]*\)<\/[[:upper:]]*>\([^<]*\)<\/\([^<]*\)><\/csc>/<\4>\1 \2 \3<\/\4>/g' $arq"_temporario5.txt" > $arq".txt"
		comparar=$(cmp $arq"_temporario5.txt" $arq".txt")
	done

	sed 's/<csc>\([^<]*\)<[[:upper:]]*>\([^<]*\)<\/[[:upper:]]*>\([^<]*\)<\/csc>/ \1 \2 \3/g' $arq".txt" > $arq"_temporario6.txt"

	comparar=$(cmp $arq".txt" $arq"_temporario6.txt")
	cat $arq"_temporario6.txt" > $arq".txt"
	

	while [[ "$comparar" != "" ]];
	do
		cat $arq".txt" > $arq"_temporario6.txt"

		sed 's/<csc>\([^<]*\)<[[:upper:]]*>\([^<]*\)<\/[[:upper:]]*>\([^<]*\)<\/csc>/\1 \2 \3/g; s/<csc>\([^<]*\)<[[:upper:]]*>\([^<]*\)<\/[[:upper:]]*>\([^<]*\)<\/\([^<]*\)><\/csc>/<\4>\1 \2 \3<\/\4>/g' $arq"_temporario6.txt" > $arq".txt"

		comparar=$(cmp $arq"_temporario6.txt" $arq".txt")
	done

	for ((cont=0; cont<${#ENTIDADES_NOMEADAS[@]}; cont++)); 
	do
		sed -i 's/<csc>\([^<]*\)<\/'${ENTIDADES_NOMEADAS[$cont]}'><\/csc>/<'${ENTIDADES_NOMEADAS[$cont]}'>\1<\/'${ENTIDADES_NOMEADAS[$cont]}'>/g' $arq".txt"
	done
	rm $arq"_temporario"*

	sed -i 's/<csc>//g; s/<\/csc>//g' $arq".txt" > ../resTemporario/$arq".txt"

	rm -r *_snt
	rm -r *_csc*	
	rm *.snt
	rm normalize.out.offsets
done

cd ..

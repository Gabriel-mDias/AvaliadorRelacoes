#!/bin/bash

#unitex="/home/likewise-open/LCAD/jupcampos/Ferramentas/Unitex/App/UnitexToolLogger"
#unitex="/home/juliana/Unitex/App/UnitexToolLogger"
unitex="/home/gabriel/Unitex-GramLab-3.1/App/UnitexToolLogger"
#dirPort="/home/likewise-open/LCAD/jupcampos/unitex/Portuguese (Brazil)"
#dirPort="/home/juliana/Unitex/Portuguese (Brazil)"
dirPort="/home/gabriel/Unitex-GramLab-3.1/Portuguese (Brazil)"


#Compila grafo grf que faz segmentação de sentença e salva o resultado em arquivo fst
	$unitex Grf2Fst2 "$dirPort/Graphs/Preprocessing/Sentence_Trabalho/Sentence.grf" -y "--alphabet=$dirPort/Alphabet.txt" -qutf8-no-bom

#Minimiza a gramática
	$unitex Flatten "$dirPort/Graphs/Preprocessing/Sentence_Trabalho/Sentence.fst2" --rtn -d5 -qutf8-no-bom

#Compila o grafo passado em linha de comando (primeiro argumento: $1) e gera o fst2 correspondente 
	$unitex Grf2Fst2 "Grafos/$1" -y "--alphabet=$dirPort/Alphabet.txt" -qutf8-no-bom

	$unitex Grf2Fst2 "$dirPort/Graphs/Preprocessing/Replace/Replace.grf" -y "--alphabet=$dirPort/Alphabet.txt" -qutf8-no-bom

cd Entrada1

for i in *txt
do
	
	#Pega nome do arquivo sem extensão .txt
	arq="${i%.*}"

	#Pega nome do grafo sem extensão .grf
	nomeGrafo="${1%.*}"

	#Cria pasta correspondente _snt
	mkdir "$arq"_snt

	#Normaliza o texto, gera o arquivo .snt
	$unitex Normalize $arq".txt" "-r../Unitex/Portuguese (Brazil)/Norm.txt" "--output_offsets="$arq"_snt/normalize.out.offsets" -qutf8-no-bom

 	#Aplica uma gramática ao texto, o texto é modificado. Aqui, está aplicando a gramática sentence.fst2 que está fazendo a segmentação em frases (adiciona os {S} no texto)
	$unitex Fst2Txt "-t"$arq".snt" "$dirPort/Graphs/Preprocessing/Sentence_Trabalho/Sentence.fst2" "-a$dirPort/Alphabet.txt" -M "--input_offsets="$arq"_snt/normalize.out.offsets" "--output_offsets="$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	$unitex Fst2Txt "-t"$arq".snt" "$dirPort/Graphs/Preprocessing/Replace/Replace.fst2" "-a$dirPort/Alphabet.txt" -R "--input_offsets="$arq"_snt/normalize.out.offsets" "--output_offsets="$arq"_snt/normalize.out.offsets" -qutf8-no-bom

	#Tokeniza o texto, gera arquivos tokens.txt, tok_by_freq.txt, tok_by_alph, stats.n, enter.pos
	$unitex Tokenize $arq".snt" "-a$dirPort/Alphabet.txt" "--input_offsets="$arq"_snt/normalize.out.offsets" "--output_offsets="$arq"_snt/tokenize.out.offsets" -qutf8-no-bom

	#Aplica dicionários(.bin) e/ou gramáticas (fst2) locais ao texto. Nesse caso, está aplicando o dicionário DELAF_PB.bin, DELACF_PB.bin e dela-en-public.bin (ingles)
	$unitex Dico "-t"$arq".snt" "-a$dirPort/Alphabet.txt" "$dirPort/Dela/DELAF_PB_2015.bin" "$dirPort/Dela/DELACF_PB.bin" "$dirPort/Dela/dela-en-public.bin" -qutf8-no-bom

	#Faz a ordenação de arquivos
	$unitex SortTxt $arq"_snt/dlf" "-l"$arq"_snt/dlf.n" "-o$dirPort/Alphabet_sort.txt" -qutf8-no-bom

 	$unitex SortTxt $arq"_snt/dlc" "-l"$arq"_snt/dlc.n" "-o$dirPort/Alphabet_sort.txt" -qutf8-no-bom

	$unitex SortTxt $arq"_snt/err" "-l"$arq"_snt/err.n" "-o$dirPort/Alphabet_sort.txt" -qutf8-no-bom

 	$unitex SortTxt $arq"_snt/tags_err" "-l"$arq"_snt/tags_err.n" "-o$dirPort/Alphabet_sort.txt" -qutf8-no-bom

	#Aplica uma gramática ao texto e constrói um índice de ocorrências (-n10000 indica que vai parar depois das 10000 primeiras ocorrências)
	$unitex Locate "-t"$arq".snt" "../Grafos/"$nomeGrafo".fst2" "-a$dirPort/Alphabet.txt" -L -M --all -b -Y --stack_max=1000 --max_matches_per_subgraph=200 --max_matches_at_token_pos=400 --max_errors=50 -qutf8-no-bom

	#Usa o arquivo concord (índice) gerado pelo locate para extrair as correspondências e substituir no próprio arquivo. 
	$unitex Concord "$arq""_snt/concord.ind" "-m$i" -qutf8-no-bom

done

rm -r *_snt
rm *.snt

cd ..


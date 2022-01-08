# usage: gawk -f tempo.awk -v TODOS=<value1> -v MODO=<value2> <ficheiro>.emir
# 
# description: Calcula a pontuacao do sistema no TEMPO nao classico.
#              

# last revision: Cristina Mota, 23.Junho.2008
# copyright: Linguateca

function update_csc(alignment, time_csc, output_string,      csc, csc_value){
#    print "------";
    match(alignment,/MaxCSC_CD\(([0-9,.]+)\) MaxCSC_S\(([0-9,.]+)\) CSC\(([0-9,.]+)\)/,csc);
    for(i=1; i<=_NCSC; i++){
#	print csc_str[i];
	sub(/\./,",",csc[i]);
	csc_value = strtonum(csc[i]) + time_csc[i];
#	print csc_value;
	if(csc_value < strtonum(csc[i])){
#	    print "VALOR MENOR: " csc_value " < " csc[i];
	}
	else{
#	    print "VALOR MAIOR: " csc_value " >= " csc[i];
	}
	csc_value = "" csc_value "";
	time_csc[i] = "" time_csc[i] "";
	sub(/,/,".",csc_value);
	sub(/,/,".",csc[i]);
	sub(/,/,".",time_csc[i]);
#	print "<<< " alignment;
	sub(csc_str[i] "[(][0-9,.]+[)]",csc_str[i] "(" csc_value ")", alignment);
#	print ">>> " alignment;
    }
    print alignment "::[{" output_string "MaxCSC_CD=MaxCSC_CD_class(" csc[_MAXCSC_CD] ")+MaxCSC_CD_tempo(" time_csc[_MAXCSC_CD] ") MaxCSC_S=MaxCSC_S_class(" csc[_MAXCSC_S] ")+MaxCSC_S_tempo(" time_csc[_MAXCSC_S] ") CSC=CSC_class(" csc[_CSC] ")+CSC_tempo(" time_csc[_CSC] ")}]" ;
#    print alignment "::[{" output_string "CSC=CSC_class(" csc[1] ")+CSC_tempo(" time_score ")}]" ;
#    print NR " ::[{" output_string "CSC(" time_score ")}]" ;
#    print $0 "::[{" output_string "CSC(" time_score ")}]" ;
}

function analyse_delta(cd_values, sys_values,      cd_converted_delta,sys_converted_delta){
    # A([0-9]+)M([0-9]+)S([0-9]+)D([0-9]+)H([0-9]+)M([0-9]+)S([0-9]+)
    cd_converted_delta=0;
    sys_converted_delta=0;
    if(cd_values[0] != sys_values[0]){
	i=7;
	while(i>=1 && max==0){
	    if(( strtonum(cd_values[i]) != 0 ) || ( strtonum(sys_values[i]) != 0 )){
		max = i;
	    }
	    i--;
	}
	for(i=1; i<=max; i++){
	    if(i!=3){
		cd_converted_delta = (cd_converted_delta + strtonum(cd_values[i]))*conv_table[i];
		sys_converted_delta = (sys_converted_delta + strtonum(sys_values[i]))*conv_table[i];
	    }
	    else{
		cd_converted_delta = cd_converted_delta + (strtonum(cd_values[i]))*conv_table[i];
		sys_converted_delta = sys_converted_delta + (strtonum(sys_values[i]))*conv_table[i];
	    }
	}
    }
    if( cd_converted_delta == sys_converted_delta ){
	return 1;
    }
    else{
	return 0;
    }
}

function analyse_val_norm_hora(cd_values, sys_values,     score_val_norm){
    # <Era><Ano><Mes><Dia>T<Hora><Minuto>E<ESTACAO>LM<limite_aberto>
    # .........T[0-9][0-9][0-9][0-9]E[A-Z][A-Z]LM[+-]
    # Os campos Ano, Dia, Hora e Minuto são considerados também como certos quando 
    # forem iguais a zero na resposta do sistema e indeterminados (valor igual a 
    # "--" ou "----") na CD, ou vice-versa.
    score_val_norm = 0;
    if(match(sys_values[0],/VAL_NORM=\"[\-+]99999999T9999E--LM-\""/)==0){
	score_val_norm = 0;
	for(i=5; i<=6; i++){
	    if(( cd_values[i] == sys_values[i] ) || 
	       (( strtonum(cd_values[i]) == 0) && (match(sys_values[i],/^-+$/) > 0)) || 
	       (( strtonum(sys_values[i]) == 0) && (match(cd_values[i],/^-+$/) > 0))){
		score_val_norm = score_val_norm + 1;
	    }
	}
	if( (cd_values[8] == sys_values[8] ) ||
	    (( match(cd_values[8],/[AD]/) > 0) && ( match(sys_values[8],/[AD]/) > 0)) ||
	    (( match(cd_values[8],/[PE]/) > 0) && ( match(sys_values[8],/[PE]/) > 0))){
	    score_val_norm = score_val_norm + 1;
	}
    }
    return score_val_norm;
}

function analyse_val_norm_data(cd_values, sys_values,     score_val_norm){
    # <Era><Ano><Mes><Dia>T<Hora><Minuto>E<ESTACAO>LM<limite_aberto>
    # .........T[0-9][0-9][0-9][0-9]E[A-Z][A-Z]LM[+-]
    # Os campos Ano, Dia, Hora e Minuto são considerados também como certos quando 
    # forem iguais a zero na resposta do sistema e indeterminados (valor igual a 
    # "--" ou "----") na CD, ou vice-versa.
    score_val_norm=0;
    if((match(cd_values[0],/VAL_NORM=\"[\-+]99999999T9999E--LM-\"/)>0) || (match(sys_values[0],/VAL_NORM=\"[\-+]99999999T9999E--LM-\"/)>0)){
	if(cd_values[0]==sys_values[0]){
	    score_val_norm = correct[att_VAL_NORM_DATA];
	}
    }
    else{
	score_val_norm = 0;
#	for(i=1;i<=8;i++){
#	    print cd_values[i] ":" sys_values[i] ":" (cd_values[i] == sys_values[i]);
#	}
	if( cd_values[1] == sys_values[1] ){
#	    print 1 ":" cd_values[1] ":" sys_values[1];
	    score_val_norm = score_val_norm + 1;
	}
	for(i=2; i<=6; i++){
#	    print i ":" cd_values[i] ":" sys_values[i];
	    if(( cd_values[i] == sys_values[i] ) || 
	       (( strtonum(cd_values[i]) == 0) && (match(sys_values[i],/^-+$/) > 0)) || 
	       (( strtonum(sys_values[i]) == 0) && (match(cd_values[i],/^-+$/) > 0))){
		score_val_norm = score_val_norm + 1;
	    }
	}
	if( cd_values[7] == sys_values[7] ){
#	    print 7 ":" cd_values[7] ":" sys_values[7];
	    score_val_norm = score_val_norm + 1;
	}
	if(( cd_values[8] == sys_values[8] ) ||
	   (( match(cd_values[8],/[AD]/) > 0) && ( match(sys_values[8],/[AD]/) > 0)) ||
	   (( match(cd_values[8],/[PE]/) > 0) && ( match(sys_values[8],/[PE]/) > 0))){
#	    print 8 ":" cd_values[8] ":" sys_values[8];
	    score_val_norm = score_val_norm + 1;
	}
    }
#    print score_val_norm;
    return score_val_norm;
}

BEGIN{
    _NCSC=3;
    _MAXCSC_CD=1;
    _MAXCSC_S=2;
    _CSC=3;
    complete=1;
    attributes=2;
    norm=3;
    csc_str[_MAXCSC_CD]="MaxCSC_CD";
    csc_str[_MAXCSC_S]="MaxCSC_S";
    csc_str[_CSC]="CSC";
    FS=" ---> ";
#     1 milenio = 1000 anos;
#     1 seculo = 100 anos;
#     1 ano = 12 meses;
#     1 mes = 30 dias;
#     1 quinzena = 14 dias;
#     1 semana = 7 dias;
#     1 dia = 24 horas;
#     1 hora = 60 minutos;
#     1 minuto = 60 segundos;
    conv_table[1]=12;
    conv_table[2]=30;
    conv_table[3]=7;
    conv_table[4]=24;
    conv_table[5]=60;
    conv_table[6]=60;
    conv_table[7]=1;

    att_TEMPO_REF="TEMPO_REF";
    att_SENTIDO="SENTIDO";
    att_VAL_DELTA="VAL_DELTA";
    att_VAL_NORM_DATA="DATA";
    att_VAL_NORM_HORA="HORA";
    att_VAL_NORM_DURACAO="DURACAO";

    weights[att_TEMPO_REF]=1-(1/3);
    weights[att_SENTIDO]=1-(1/5);
    weights[att_VAL_DELTA]=1;
    weights[att_VAL_NORM_DATA]=1/8;
    weights[att_VAL_NORM_HORA]=1/3;
    weights[att_VAL_NORM_DURACAO]=1;

    correct[att_TEMPO_REF]=1;
    correct[att_SENTIDO]=1;
    correct[att_VAL_DELTA]=1;
    correct[att_VAL_NORM_DATA]=8;
    correct[att_VAL_NORM_HORA]=3;
    correct[att_VAL_NORM_DURACAO]=1;

    for(i=1;i<=_NCSC; i++){
	time_score[i]=0;
    }
    output_string = "";
}
/^#/||/^DOC /||/^$/{
    print $0;
    next;
}
{
    output_string = "";
    for(i=1;i<=_NCSC; i++){
	time_score[i]=0;
    }
}
#### MaxCSC_S
/---> .+TEMPO_REF/ && ((MODO==complete)||(MODO==attributes)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_TEMPO_REF];
}
/---> .*SENTIDO/ && ((MODO==complete)||(MODO==attributes)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_SENTIDO];
}
/---> .*SUBTIPO=\"DATA\".* VAL_NORM/ && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_VAL_NORM_DATA] * correct[att_VAL_NORM_DATA];
}
/---> .*SUBTIPO=\"HORA\".* VAL_NORM=/ && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_VAL_NORM_HORA] * correct[att_VAL_NORM_HORA];
}
/---> .*TIPO=\"DURACAO\".* VAL_NORM/ && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_VAL_NORM_DURACAO];
}
/---> .*VAL_DELTA/ && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_S] = time_score[_MAXCSC_S] + weights[att_VAL_DELTA];
}
/TEMPO_REF=.* ---> / && ((MODO==complete)||(MODO==attributes)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_TEMPO_REF];
    match($1,/TEMPO_REF=\"([^"]+)\"/,valcd);
    if(match($2,/TEMPO_REF=\"([^"]+)\"/,valsys)>0){
	if( valcd[1] == valsys[1] ){
	    time_score[_CSC] = time_score[_CSC] + weights[att_TEMPO_REF] * 1;
	    output_string = output_string "TempoRef(Correcto:[" valcd[1] "] Em_Falta:[]) ";
	}
	else{
	    output_string = output_string "TempoRef(Correcto:[] Em_Falta:[" valcd[1] "]) ";
	    print NR ":" output_string > FILENAME ".ERROS.txt";
	}
    }
    else{
	output_string = output_string "TempoRef(Correcto:[] Em_Falta:[" valcd[1] "]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
}
/SENTIDO=.* ---> / && ((MODO==complete)||(MODO==attributes)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_SENTIDO];
    match($1,/SENTIDO=\"([^"]+)\"/,valcd);
    if(match($2,/SENTIDO=\"([^"]+)\"/,valsys)>0){
	if( valcd[1] == valsys[1] ){
	    time_score[_CSC] = time_score[_CSC] + weights[att_SENTIDO] * 1;
	    output_string = output_string "Sentido(Correcto:[" valcd[1] "] Em_Falta:[]) ";
	}
	else{
	    output_string = output_string "Sentido(Correcto:[] Em_Falta:[" valcd[1] "]) ";
	    print NR ":" output_string > FILENAME ".ERROS.txt";
	}
    }
    else{
	output_string = output_string "Sentido(Correcto:[] Em_Falta:[" valcd[1] "]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
}
/VAL_DELTA=\"\".* ---> / && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_DELTA];
    if(match($2,/VAL_DELTA=\"\"/)>0){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_DELTA] * 1;
	output_string = output_string "ValDelta(Correcto:[\"\"] Em_Falta:[]) ";
    }
    else{
	output_string = output_string "ValDelta(Correcto:[] Em_Falta:[\"\"]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
    update_csc($0, time_score, output_string);
    next;
}
/SUBTIPO=\"[DH][A-Z]+\".*VAL_NORM=\"\".* --->/ && ((MODO==complete)||(MODO==norm)){
    match($1,/SUBTIPO=\"([DH][A-Z]+)\".*VAL_NORM=\"\"/,cd_norm_type);
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[cd_norm_type[1]] * correct[cd_norm_type[1]];
    if(match($2,/SUBTIPO=\"([DH][A-Z]+)\".*VAL_NORM=\"\"/,sys_norm_type)>0){
	time_score[_CSC] = time_score[_CSC] + weights[sys_norm_type[1]] * correct[sys_norm_type[1]];
	output_string = output_string "ValNorm(Correcto:[\"\"] Em_Falta:[]) ";
    }
    else{
	output_string = output_string "ValNorm(Correcto:[] Em_Falta:[\"\"]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
    update_csc($0, time_score, output_string);
    next;
}
/TIPO=\"DURACAO\".*VAL_NORM=\"\".* --->/ && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_NORM_DURACAO] * correct[att_VAL_NORM_DURACAO];
    if(match($2,/TIPO=\"DURACAO\".*VAL_NORM=\"\"/)>0){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_DURACAO] * correct[att_VAL_NORM_DURACAO];
	output_string = output_string "ValNorm(Correcto:[\"\"] Em_Falta:[]) ";
    }
    else{
	output_string = output_string "ValNorm(Correcto:[] Em_Falta:[\"\"]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
    update_csc($0, time_score, output_string);
    next;
}
/VAL_DELTA=.* ---> / && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_DELTA];
    match($1,/VAL_DELTA=\"A([0-9]+)M([0-9]+)S([0-9]+)D([0-9]+)H([0-9]+)M([0-9]+)S([0-9]+)\"/,valcd);
    if(match($2,/VAL_DELTA=\"A([0-9]+)M([0-9]+)S([0-9]+)D([0-9]+)H([0-9]+)M([0-9]+)S([0-9]+)\"/,valsys)>0){
	val_match = analyse_delta(valcd,valsys);
    }
    else{
	val_match;
    }
    sub(/VAL_DELTA=\"/,"",valcd[0]);
    sub(/\"/,"",valcd[0]);
    if( val_match == 1){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_DELTA] * val_match;
	output_string = output_string "ValDelta(Correcto:[" valcd[0] "] Em_Falta:[]) ";
    }
    else{
	output_string = output_string "ValDelta(Correcto:[] Em_Falta:[" valcd[0] "]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
    update_csc($0, time_score, output_string);
    next;
}
/VAL_NORM=\"A.* ---> / && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_NORM_DURACAO];
    match($1,/VAL_NORM=\"A([0-9]+)M([0-9]+)S([0-9]+)D([0-9]+)H([0-9]+)M([0-9]+)S([0-9]+)\"/,valcd);
    if(match($2,/VAL_NORM=\"A([0-9]+)M([0-9]+)S([0-9]+)D([0-9]+)H([0-9]+)M([0-9]+)S([0-9]+)\"/,valsys)>0){
	val_match =  analyse_delta(valcd,valsys);
    }
    else{
	val_match;
    }
    sub(/VAL_NORM=\"/,"",valcd[0]);
    sub(/\"/,"",valcd[0]);
    if( val_match == 1 ){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_DURACAO] * val_match;
	output_string = output_string "ValNorm(Correcto:[" valcd[0] "] Em_Falta:[]) ";
    }
    else{
	output_string = output_string "ValNorm(Correcto:[] Em_Falta:[" valcd[0] "]) ";
	print NR ":" output_string > FILENAME ".ERROS.txt";
    }
    update_csc($0, time_score, output_string);
    next;
}
/HORA.*VAL_NORM=.* ---> / && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_NORM_HORA] * correct[att_VAL_NORM_HORA];
    match($1,/VAL_NORM=\"(.)(....)(..)(..)T(..)(..)E(..)LM(.)\"/,valcd);
    if(match($2,/VAL_NORM=\"(.)(....)(..)(..)T(..)(..)E(..)LM(.)\"/,valsys)>0){
	val_match = analyse_val_norm_hora(valcd,valsys);
    }
    else{
	val_match=0;
    }
    sub(/VAL_NORM=\"/,"",valcd[0]);
    sub(/\"/,"",valcd[0]);
    if( val_match == correct[att_VAL_NORM_HORA] ){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_HORA] * val_match;
	output_string = output_string "ValNorm(Correcto:[" valcd[0] "] Em_Falta:[]) ";
    }
    else{
	if(val_match > 0){
	    time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_HORA] * val_match;
	    output_string = output_string "ValNorm(CorrectoP:[" valcd[0] "] Em_Falta:[]) ";
	}
	else{
	    output_string = output_string "ValNorm(Correcto:[] Em_Falta:[" valcd[0] "]) ";
	    print NR ":" output_string > FILENAME ".ERROS.txt";
	}
    }
    update_csc($0, time_score, output_string);
    next;
}
/DATA.*VAL_NORM=.* ---> / && ((MODO==complete)||(MODO==norm)){
    time_score[_MAXCSC_CD] = time_score[_MAXCSC_CD] + weights[att_VAL_NORM_DATA] * correct[att_VAL_NORM_DATA];
    match($1,/VAL_NORM=\"(.)(....)(..)(..)T(..)(..)E(..)LM(.)\"/,valcd);
    if(match($2,/VAL_NORM=\"(.)(....)(..)(..)T(..)(..)E(..)LM(.)\"/,valsys)>0){
	val_match = analyse_val_norm_data(valcd,valsys);
    }
    else{
	val_match=0;
    }
    sub(/VAL_NORM=\"/,"",valcd[0]);
    sub(/\"/,"",valcd[0]);
    if( val_match == correct[att_VAL_NORM_DATA] ){
	time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_DATA] * val_match;
	output_string = output_string "ValNorm(Correcto:[" valcd[0] "] Em_Falta:[]) ";
    }
    else{
	if(val_match > 0){
	    time_score[_CSC] = time_score[_CSC] + weights[att_VAL_NORM_DATA] * val_match;
	    output_string = output_string "ValNorm(CorrectoP:[" valcd[0] "] Em_Falta:[]) ";
	}
	else{
	    output_string = output_string "ValNorm(Correcto:[] Em_Falta:[" valcd[0] "]) ";
	    print NR ":" output_string > FILENAME ".ERROS.txt";
	}
    }
    update_csc($0, time_score, output_string);
    next;
}
/CATEG=\"[^"]*TEMPO[^"]*\".* --->/{
    update_csc($0, time_score, output_string);
    next;
}
{
    if((TODOS==1) || (TODOS=="")){
	print $0;
    }
}


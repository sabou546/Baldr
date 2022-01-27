;;; Programme qui dit si une chaine lue au clavier est un palindrome
;;; 
;;; Exercice 3
;;; 
;;; Auteur: Eric Wenaas
;;; 
segment .data
m_invite:	db "Entrez une chaine            : "
l_invite:	equ $ - m_invite
m_palind:	db "C'est un palindrome", 10
l_palind:	equ $ - m_palind
m_npalind:	db "Ce n'est pas un palindrome", 10
l_npalind:	equ $ - m_npalind
palindrome	dd 1    	; C'est un palindrome par defaut
segment .bss
chaine:		resb 17	; Un de plus pour le retour
longueur	resd 1	; Longueur de la chaine
segment .text
	global _start
_start:	
b_princ:
	;; Ecriture de l'invite a entrer une chaine
	mov eax, 4
	mov ebx, 1
	mov ecx, m_invite
	mov edx, l_invite
	int 0x80
	;; Saisie de la chaine
	mov eax, 3
	mov ebx, 0
	mov ecx, chaine
	mov edx, 17
	int 0x80
	dec eax			  ; On ignore le retour pour l'analyse
	mov dword [longueur], eax ; On conserve le nombre de caracteres lus
	mov ecx, eax		  ; Pour boucler
	cmp ecx, 0
	je  aff_res		  ; Si aucun caractere a inverser
;;; On empile les caracteres sur la pile
	mov ebx, chaine
	xor edx, edx
b_empile:
	mov dl, [ebx]
	push edx
	inc ebx
	loop b_empile
;;; On depile les caracteres de la pile
;;; en comparant avec les caracteres de la chaine
	mov ebx, chaine
	mov ecx, [longueur]
b_depile:
	pop edx
	cmp [ebx], dl 		; meme caractere
	je  suite
	mov dword [palindrome], 0 ; non --> ce n'est pas un palindrome
suite:	inc ebx			; passe au suivant
	loop b_depile
aff_res:
	;; Verification si c'est un palindrome
	cmp dword [palindrome], 1
	jne non_palind
	mov ecx, m_palind
	mov edx, l_palind
	jmp affiche
non_palind:
	mov ecx, m_npalind
	mov edx, l_npalind
affiche
	mov eax, 4
	mov ebx, 1
	int 0x80
quitter:
	mov eax, 1
	mov ebx, 0
	int 0x80
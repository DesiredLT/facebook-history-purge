from pathlib import Path

files=[
    Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java'),
    Path('app/src/main/java/lt/vaeloria/ooc/GroqClient.java')
]

replacements={
    'post-cap meistriškumas':'meistriškumas virš bazinės ribos',
    'post-cap specializacija':'specializacija virš bazinės ribos',
    'post-cap poveikis':'poveikis virš bazinės ribos',
    'post-cap':'virš bazinės ribos',
    'PERŽIŪRĖTI VISAS 92 SAVYBES IR XP':'PERŽIŪRĖTI VISAS 92 SAVYBES IR PATIRTĮ',
    'situacinis patikros bonusas':'situacinis patikros pranašumas'
}

for p in files:
    s=p.read_text(encoding='utf-8')
    for old,new in replacements.items():
        s=s.replace(old,new)
    p.write_text(s,encoding='utf-8')

# UI gate: these terms must no longer appear in the generated main screen.
main=files[0].read_text(encoding='utf-8')
for forbidden in ['VIETINIS RPG','post-cap',' IR XP']:
    if forbidden in main:
        raise SystemExit('Likęs anglicizmas pagrindiniame ekrane: '+forbidden)

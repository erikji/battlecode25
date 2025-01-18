from itertools import product
import re
import subprocess
import platform
from time import gmtime, strftime, time

emojiMode = True
emojiMap = {
    'Won': ':white_check_mark:',
    'Lost': ':x:',
    'Tied': ':grimacing:',
    'N/A': ':heavy_minus_sign:',
    'Error': ':heavy_exclamation_mark:'
}
errors = []

currentBot = 'a' #bot to test
#SPAARK IS THE BASELINE

# bots = [chr(ord('a')+i) for i in range(1)]
bots = ["SPAARK"]
# bots = ['MASON']

maps = []
maps += ['DefaultSmall']
maps += ['DefaultMedium']
maps += ['DefaultLarge']
maps += ['DefaultHuge']

maps += ['Fossil']
maps += ['Gears']
maps += ['Justice']
maps += ['Mirage']
maps += ['Money']
maps += ['MoneyTower']
maps += ['Racetrack']
maps += ['Restart']
# 42% +1 -1 mopperpoi2
maps += ['SMILE']
maps += ['SaltyPepper']
maps += ['TargetPractice']
maps += ['Thirds']
maps += ['UglySweater']
maps += ['UnderTheSea']
maps += ['catface']
maps += ['gardenworld']
maps += ['memstore']

maps += ['[EDGE] microtest']
maps += ['[SP] ChessBattleAdvanced']
maps += ['[EDGE] srp spam']
# maps += ['[SP2] Duels']
maps += ['[SP2] Boulders']
maps += ['[SP2] Arena']
maps += ['[SP2] CheckersFightIntermediate']
maps += ['[EDGE] ruins']
maps += ['[SP] ChessBattleUnadvanced']
maps += ['[SP2] Geometric']
maps += ['[SP] RoundAbout']
maps += ['[SP] 2025']
maps += ['[SP] 5757']
maps += ['[SP] Lingo']
maps += ['[SP] Longo']
maps += ['[SP] MapIsNotReference']
maps += ['[SP] ModernArt3']
maps += ['[SP] Spirals']
maps += ['[SP] Squiggles']
# maps += ['[SP] Stars']
maps += ['[SP2] Piston']
maps += ['[SP] Cornucopia']
maps += ['[SP2] Buh']
maps += ['[SP2] Arena2']
maps += ['[SP2] CenterExtreme']
maps += ['[SP2] Corners']
maps += ['[SP2] Duolingo']
maps += ['[SP2] Narrow']
maps += ['[SP2] NotMyMap']

#maps.reverse()
matches = list(product(bots, maps))

numWinsMapping = {
    0: 'Lost',
    1: 'Tied',
    2: 'Won',
}

def retrieveGameLength(output):
    startIndex = output.find('wins (round ')
    if startIndex == -1:
        return -1
    endIndex = output.find(')', startIndex)
    if endIndex == -1:
        return -1
    return output[startIndex + len('wins(round ') + 1:endIndex]

def run_match(bot, map):
    try:
        if platform.system() == 'Windows':
            outputA = str(subprocess.check_output(['gradlew', 'run', '-PteamA=' + currentBot, '-PteamB=' + bot, '-Pmaps=' + map], shell=True))
            outputB = str(subprocess.check_output(['gradlew', 'run', '-PteamA=' + bot, '-PteamB=' + currentBot, '-Pmaps=' + map], shell=True))
        else:
            outputA = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + currentBot, '-PteamB=' + bot, '-Pmaps=' + map]))
            outputB = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + bot, '-PteamB=' + currentBot, '-Pmaps=' + map]))
    except subprocess.CalledProcessError as exc:
        print("Status: FAIL", exc.returncode, exc.output)
        return 'Error'
    else:
        winAString = '{} (A) wins'.format(currentBot)
        winBString = '{} (B) wins'.format(currentBot)
        loseAString = '{} (B) wins'.format(bot)
        loseBString = '{} (A) wins'.format(bot)
        resignedString = 'resigned'
        
        numWins = 0
        
        gameLengthA = retrieveGameLength(outputA)
        gameAResigned = resignedString in outputA
        gameLengthB = retrieveGameLength(outputB)
        gameBResigned = resignedString in outputB

        flagRegex = "FLAG{[^{}]*}"
        gameAFlags = list(set(re.findall(flagRegex, outputA)))
        gameBFlags = list(set(re.findall(flagRegex, outputB)))
        gameAFlags = ", ".join([s[5:-1] for s in gameAFlags])
        gameBFlags = ", ".join([s[5:-1] for s in gameBFlags])
        if len(gameAFlags) > 0:
            gameAFlags = "{{{}}}".format(gameAFlags)
        if len(gameBFlags) > 0:
            gameBFlags = "{{{}}}".format(gameBFlags)

        gameAInfo = gameLengthA + ('*' if gameAResigned else '') + gameAFlags
        gameBInfo = gameLengthB + ('*' if gameBResigned else '') + gameBFlags
        
        if winAString in outputA:
            numWins += 1
        else:
            if not loseAString in outputA:
                return 'Error'
        if winBString in outputB:
            numWins += 1
        else:
            if not loseBString in outputB:
                return 'Error'
        return (numWinsMapping[numWins] + ' (' + ', '.join([gameAInfo, gameBInfo]) + ')', numWins)

results = {}
ctr = 0
#run matches
currentTime = time()
for i in range(len(bots)):
    bot = bots[i]
    winsThisBot = 0
    for j in range(len(maps)):
        map = maps[j]
        ctr = ctr + 1
        print("(" + strftime("%H:%M:%S", gmtime()) + ") " + str(ctr) + " of " + str(len(bots)*len(maps)) + ": {} vs {} on {}".format(currentBot, bot, map), end=" ", flush=True)
        results[(bot, map)], wins = run_match(bot, map)
        winsThisBot += wins
        print(currentBot + " won " + str(round(winsThisBot / (j*2 + 2) * 100)) + "% against " + bot)
    print(currentBot + " won " + str(winsThisBot) + " of " + str(len(maps)*2) + " against " + bot + "\n")

# Construct table
table = [[results.get((bot, map), 'N/A') for bot in bots] for map in maps]

def replaceWithDictionary(s, mapping):
    for a, b in mapping.items():
        s = s.replace(a, b)
    return s

if emojiMode:
    table = [[replaceWithDictionary(item, emojiMap) for item in row] for row in table]

# Write to file
with open('matches-summary.txt', 'w') as f:
    table = [[''] + bots, [':---:' for i in range(len(bots) + 1)]] + [[map] + row for map, row in zip(maps, table)]
    for line in table:
        f.write('| ')
        f.write(' | '.join(line))
        f.write(' |')
        f.write('\n')
    f.write('\n')
    for error in errors:
        f.write(error)

print("Took " + str((time() - currentTime) / 60) + " minutes")
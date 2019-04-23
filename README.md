# JokeServer
Server sends a random joke to a client after client sends a request
- Maintains state so that all jokes/proverbs are seen before any are repeated.
- Tested with this many clients simultaneously (more than one!)
- Returns four jokes and four proverbs
- Jokes and Proverbs re-randomized after each cycle
- Admin Client connects at different port
- Admin client switches server mode from Joke Mode to Proverb Mode
- Maintains client conversations without interference from other clients
- Only have to type user name once on JokeClient
- Can start and stop JokeClientAdmin at any time
- Can run JokeServer and JokeClient without JokeClientAdmin

 

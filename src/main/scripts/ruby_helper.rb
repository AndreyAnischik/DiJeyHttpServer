require 'json'

def post_change (args)
  json_args = JSON.parse(args)
  "You have changed #{json_args["team"]} team."
end

def fake_post (args)
  loop do
    puts "Processing args #{args}."
  end
end

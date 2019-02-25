def post_change (*args)
  desired_data = args.first
  hash = {}
  desired_data.each { |key, value| hash[key] = value }
  "You have changed #{hash["team"]} team."
end

def fake_post (*args)
  loop do
    puts "Processing args #{args}."
  end
end

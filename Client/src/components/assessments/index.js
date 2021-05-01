import { FilmOutline as Film } from '@graywolfai/react-heroicons'
import { useState } from 'react'
import { Button } from '../'

export function Assessments({ comments, createComment, film }) {
    let render = []

    render.push(<ObtainComments comments={comments} />)
    render.push(<CreateComment createComment={createComment} film={film}/>)

    return render
}

function createRating(rating) {
    let children = []

    for (let i = 0; i < 10; i++) {
        if (i < rating) {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`} />);
        } else {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`} />);
        }
    }

    return children
}

function ObtainComments({ comments }) {
    let render = <></>

    if (comments.content != null) {
        render = comments.content.map((comment) =>
            <div key={comment.id} className='h-96 w-4/6 bg-white rounded p-4 flex flex-col shadow-md border-2'>
                <div className='ml-8 mt-4 flex justify-between'>
                    <span className='font-bold'>{comment.user.email}</span>
                    <div className='text-right mr-10'>
                        {
                            createRating(comment.rating)
                        }
                    </div>
                </div>
                <p className='p-8 md:overflow-hidden'>{comment.comment}</p>
            </div>
        );
    }

    return render
}

function Ratings({ ratings, setRating }) {
    return <div className='mt-4'>
        {
            [...Array(10)].map((v, i) => (
                i < ratings
                    ?
                    <Film
                        className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`}
                        onClick={() => setRating(i + 1)}
                    />
                    :
                    <Film
                        className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`}
                        onClick={() => setRating(i + 1)}
                    />
            ))
        }
    </div>
}

function CreateComment({ createComment, film }) {
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState('');

    const submit = async (event) => {
        await createComment({
            user: localStorage.getItem('user'),
            film: film,
            comment: comment,
            rating: rating
        })
        setRating(0)
        //NO BORRA EL COMENTARIO DEL TEXTAREA
        setComment('')
    }

    return <div className='w-full h-64 mt-10 flex justify-start'>
        <div className='flex flex-col md:w-64'>
            <p className='font-bold'>Y a ti, ¿qué te ha parecido?</p>
            <Ratings ratings={rating} setRating={setRating} />
            <Button className='mt-32' type='submit' variant='primary' onClick={submit}>Publicar</Button>
        </div>
        <textarea name='search'
            type='text'
            placeholder='Escribe aquí tu comentario y comparte tu opinión con otros usuarios! Pero por favor, evita hacer spoilers...'
            className='md:p-4 ml-8 bg-white rounded placeholder-gray-400 font-medium border w-full'
            onChange={(event) => setComment(event.target.value)}
        />
    </div>
}